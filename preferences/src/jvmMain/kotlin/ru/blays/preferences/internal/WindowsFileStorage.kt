package ru.blays.preferences.internal

import androidx.annotation.GuardedBy
import androidx.datastore.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

class WindowsFileStorage<T>(
    private val serializer: Serializer<T>,
    private val coordinatorProducer: (File) -> InterProcessCoordinator = {
        SingleProcessCoordinator()
    },
    private val produceFile: () -> File
) : Storage<T> {

    override fun createConnection(): StorageConnection<T> {
        val file = produceFile()

        synchronized(activeFilesLock) {
            val path = file.absolutePath
            check(!activeFiles.contains(path)) {
                "There are multiple DataStores active for the same file: $path. You should " +
                        "either maintain your DataStore as a singleton or confirm that there is " +
                        "no two DataStore's active on the same file (by confirming that the scope" +
                        " is cancelled)."
            }
            activeFiles.add(path)
        }

        return FileStorageConnection(file, serializer, coordinatorProducer(file)) {
            synchronized(activeFilesLock) {
                activeFiles.remove(file.absolutePath)
            }
        }
    }

    internal companion object {
        /**
         * Active files should contain the absolute path for which there are currently active
         * DataStores. A DataStore is active until the scope it was created with has been
         * cancelled. Files aren't added to this list until the first read/write because the file
         * path is computed asynchronously.
         */
        @GuardedBy("activeFilesLock")
        internal val activeFiles = mutableSetOf<String>()

        internal val activeFilesLock = Any()
    }
}

internal class FileStorageConnection<T>(
    private val file: File,
    private val serializer: Serializer<T>,
    override val coordinator: InterProcessCoordinator,
    private val onClose: () -> Unit
) : StorageConnection<T> {

    private val closed = AtomicBoolean(false)
    // TODO:(b/233402915) support multiple readers
    private val transactionMutex = Mutex()

    override suspend fun <R> readScope(
        block: suspend ReadScope<T>.(locked: Boolean) -> R
    ): R {
        checkNotClosed()

        val lock = transactionMutex.tryLock()
        try {
            return FileReadScope(file, serializer).use {
                block(it, lock)
            }
        } finally {
            if (lock) {
                transactionMutex.unlock()
            }
        }
    }

    @Suppress("NewApi")
    override suspend fun writeScope(block: suspend WriteScope<T>.() -> Unit) {
        checkNotClosed()
        file.createParentDirectories()

        transactionMutex.withLock {
            val scratchFile = File(file.absolutePath + ".tmp")
            try {
                FileWriteScope(scratchFile, serializer).use {
                    block(it)
                }
                val isFileMoved: Boolean = try {
                    Files.move(scratchFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                if (scratchFile.exists() && !isFileMoved) {
                    throw IOException(
                        "Unable to rename $scratchFile. " +
                                "This likely means that there are multiple instances of DataStore " +
                                "for this file. Ensure that you are only creating a single instance of " +
                                "datastore for this file."
                    )
                }
            } catch (ex: IOException) {
                if (scratchFile.exists()) {
                    scratchFile.delete() // Swallow failure to delete
                }
                throw ex
            }
        }
    }

    public override fun close() {
        closed.set(true)
        onClose()
    }

    private fun checkNotClosed() {
        check(!closed.get()) { "StorageConnection has already been disposed." }
    }

    private fun File.createParentDirectories() {
        val parent: File? = canonicalFile.parentFile

        parent?.let {
            it.mkdirs()
            if (!it.isDirectory) {
                throw IOException("Unable to create parent directories of $this")
            }
        }
    }
}

internal open class FileReadScope<T>(
    protected val file: File,
    protected val serializer: Serializer<T>
) : ReadScope<T> {

    private val closed = AtomicBoolean(false)

    override suspend fun readData(): T {
        checkNotClosed()
        return try {
            withContext(Dispatchers.IO) {
                file.inputStream().use { stream ->
                    serializer.readFrom(stream)
                }
            }
        } catch (ex: FileNotFoundException) {
            if (file.exists()) {
                // Re-read to prevent throwing from a race condition where the file is created by
                // another process after the initial read attempt but before `file.exists()` is
                // called. Otherwise file exists but we can't read it; throw FileNotFoundException
                // because something is wrong.
                return withContext(Dispatchers.IO) {
                    file.inputStream().use { stream ->
                        serializer.readFrom(stream)
                    }
                }
            }
            return serializer.defaultValue
        }
    }

    override fun close() {
        closed.set(true)
    }
    protected fun checkNotClosed() {
        check(!closed.get()) { "This scope has already been closed." }
    }
}

internal class FileWriteScope<T>(file: File, serializer: Serializer<T>) :
    FileReadScope<T>(file, serializer), WriteScope<T> {

    override suspend fun writeData(value: T) {
        checkNotClosed()
        val fos = withContext(Dispatchers.IO) {
            file.outputStream()
        }
        fos.use { stream ->
            serializer.writeTo(value, UncloseableOutputStream(stream))
            stream.fd.sync()
            // TODO(b/151635324): fsync the directory, otherwise a badly timed crash could
            //  result in reverting to a previous state.
        }
    }
}

private class UncloseableOutputStream(val fileOutputStream: FileOutputStream) : OutputStream() {

    override fun write(b: Int) {
        fileOutputStream.write(b)
    }

    override fun write(b: ByteArray) {
        fileOutputStream.write(b)
    }

    override fun write(bytes: ByteArray, off: Int, len: Int) {
        fileOutputStream.write(bytes, off, len)
    }

    override fun close() {
        // We will not close the underlying FileOutputStream until after we're done syncing
        // the fd. This is useful for things like b/173037611.
    }

    override fun flush() {
        fileOutputStream.flush()
    }
}