package ru.blays.ficbook.reader.shared.data.cookieStorage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import ru.blays.ficbook.reader.shared.data.realm.entity.CookieEntity
import ru.blays.ficbook.reader.shared.di.injectRealm


interface MutableCookieJar: CookieJar {
    fun addAll(cookies: List<Cookie>)
    fun getAll(): List<Cookie>
    suspend fun clearAll()
}

class DynamicCookieJar: ru.blays.ficbook.reader.shared.data.cookieStorage.MutableCookieJar {
    private val storage: MutableList<Cookie> = mutableListOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun clearAll() {
        storage.clear()
        val realm by injectRealm()
        realm.write {
            val saved = query(CookieEntity::class).find()
            delete(saved)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return storage
    }

    override fun addAll(cookies: List<Cookie>) {
        storage.clear()
        storage.addAll(cookies)
    }

    override fun getAll(): List<Cookie> {
        return storage
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        coroutineScope.launch {
            cookies.forEach { cookie ->
                if(cookie.name == "PHPSESSID" || cookie.name == "rme") {
                    try {
                        if(storage.isNotEmpty()) {
                            storage.removeIf { it.name == cookie.name }
                        }
                        storage.add(cookie)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

fun Cookie.toEntity() = CookieEntity(
    name = name,
    value = value,
    domain = domain,
    path = path,
    secure = secure
)

fun CookieEntity.toCookie(): Cookie {
    val builder = Cookie.Builder()
    builder.name(name)
    builder.value(value)
    domain?.let { builder.domain(it) }
    path?.let { builder.path(it) }
    if(secure) builder.secure()
    return builder.build()
}