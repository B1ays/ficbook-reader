@file:JvmName("FilePickerJvmKt")
package com.darkrockstudios.libraries.mpfilepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File

public actual data class PlatformFile(
	val file: File,
)

@Composable
public actual fun FilePicker(
	show: Boolean,
	initialDirectory: String?,
	fileExtensions: List<String>,
	title: String?,
	onFileSelected: FileSelected,
) {
	LaunchedEffect(show) {
		if (show) {
			val fileFilter = if (fileExtensions.isNotEmpty()) {
				fileExtensions.joinToString(",")
			} else {
				""
			}

			val initialDir = initialDirectory ?: System.getProperty("user.dir")
			val filePath = chooseFile(
				initialDirectory = initialDir,
				fileExtension = fileFilter,
				title = title
			)
			if (filePath != null) {
				val file = File(filePath)
				val platformFile = PlatformFile(file)
				onFileSelected(platformFile)
			} else {
				onFileSelected(null)
			}

		}
	}
}

@Composable
public actual fun MultipleFilePicker(
	show: Boolean,
	initialDirectory: String?,
	fileExtensions: List<String>,
	title: String?,
	onFileSelected: FilesSelected
) {
	LaunchedEffect(show) {
		if (show) {
			val fileFilter = if (fileExtensions.isNotEmpty()) {
				fileExtensions.joinToString(",")
			} else {
				""
			}

			val initialDir = initialDirectory ?: System.getProperty("user.dir")
			val filePaths = chooseFiles(
				initialDirectory = initialDir,
				fileExtension = fileFilter,
				title = title
			)
			if (filePaths != null) {
				onFileSelected(filePaths.map { PlatformFile(File(it)) })
			} else {
				onFileSelected(null)
			}

		}
	}
}

@Composable
public actual fun DirectoryPicker(
	show: Boolean,
	initialDirectory: String?,
	title: String?,
	onFileSelected: (String?) -> Unit,
) {
	LaunchedEffect(show) {
		if (show) {
			val initialDir = initialDirectory ?: System.getProperty("user.dir")
			val fileChosen = chooseDirectory(initialDir, title)
			onFileSelected(fileChosen)
		}
	}
}

@Composable
actual fun FileSaver(
	show: Boolean,
	fileExtension: String,
	fileName: String,
	title: String?,
	onFileSelected: FileSelected,
) {

	LaunchedEffect(show) {
		if(show) {
			val initialDir = System.getProperty("user.dir")
			val filePath = saveFile(initialDir, fileName, fileExtension, title)
			if (filePath != null) {
				val file = File(filePath)
				val platformFile = PlatformFile(file)
				onFileSelected(platformFile)
			} else {
				onFileSelected(null)
			}
		}
	}
}