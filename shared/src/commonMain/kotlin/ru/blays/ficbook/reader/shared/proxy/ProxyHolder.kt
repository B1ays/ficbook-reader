package ru.blays.ficbook.reader.shared.proxy

import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.Authenticator
import okhttp3.Credentials
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import java.io.IOException
import java.net.*

interface IProxyHolder {
    fun setUpConfig(
        config: ProxyConfig
    )

    fun disable()
}

@OptIn(ExperimentalSerializationApi::class)
class ProxyHolder private constructor() : ProxySelector(), IProxyHolder {
    private var currentProxyList: List<Proxy>? = null
    private var passwordAuthentication: PasswordAuthentication? = null
    private var credentials: String? = null

    constructor(settings: Settings) : this() {
        val enabled = settings.getBoolean(SettingsKeys.PROXY_ENABLED_KEY, true)

        if(enabled) {
            val useCustomProxy = settings.getBoolean(SettingsKeys.PROXY_USE_CUSTOM_KEY, false)
            if(useCustomProxy) {
                val config = settings.decodeValueOrNull(
                    ProxyConfig.serializer(),
                    SettingsKeys.PROXY_CONFIG_KEY
                )
                if(config != null) {
                    setUpConfig(config)
                }
            } else {
                setUpConfig(defaultProxyConfig)
            }
        }

        java.net.Authenticator.setDefault(
            object : java.net.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return this@ProxyHolder.passwordAuthentication
                        ?: super.getPasswordAuthentication()
                }
            }
        )
    }

    val authenticator = Authenticator { _, response ->
        if (credentials == null) {
            return@Authenticator null
        }
        if (response.request.header("Proxy-Authorization") != null) {
            return@Authenticator null
        }
        return@Authenticator response.request.newBuilder()
            .header(
                "Proxy-Authorization",
                requireNotNull(credentials)
            )
            .build()
    }

    override fun setUpConfig(config: ProxyConfig) {
        currentProxyList = listOf(
            createProxyForConfig(config)
        )

        val userName = config.username
        val password = config.password
        if (userName != null && password != null) {
            when(config.type) {
                Proxy.Type.DIRECT -> Unit
                Proxy.Type.HTTP -> {
                    credentials = Credentials.basic(userName, password)
                    passwordAuthentication = null
                }
                Proxy.Type.SOCKS -> {
                    passwordAuthentication = PasswordAuthentication(userName, password.toCharArray())
                    credentials = null
                }
            }
        }
    }

    override fun disable() {
        currentProxyList = null
        passwordAuthentication = null
        credentials = null
    }

    override fun select(uri: URI?): List<Proxy> {
        return if (currentProxyList != null) {
            requireNotNull(currentProxyList)
        } else {
            noProxyList
        }
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        println("ProxyHandler: Connection failed, uri: $uri")
    }

    companion object {
        private val noProxyList = listOf(Proxy.NO_PROXY)
    }
}