package ru.blays.ficbook.reader.shared.preferences.json

import kotlinx.serialization.Serializable
import java.net.Proxy

@Serializable
data class ProxyConfig(
    val hostname: String,
    val port: Int,
    val type: Proxy.Type,
    val username: String?,
    val password: String?,
) {
    class Builder(
        private val hostname: String,
        private val port: Int,
        private val type: Proxy.Type
    ) {
        private var username: String? = null
        private var password: String? = null

        fun username(username: String?) = apply { this.username = username }
        fun password(password: String?) = apply { this.password = password }

        fun build() = ProxyConfig(
            hostname = hostname,
            port = port,
            type = type,
            username = username,
            password = password
        )
    }

    companion object {
        fun build(
            hostname: String,
            port: Int,
            type: Proxy.Type,
            builder: Builder.() -> Unit,
        ): ProxyConfig {
            return Builder(
                hostname = hostname,
                port = port,
                type = type
            ).apply(builder).build()
        }
    }
}
