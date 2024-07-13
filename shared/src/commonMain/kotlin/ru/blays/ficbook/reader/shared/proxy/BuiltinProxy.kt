package ru.blays.ficbook.reader.shared.proxy

import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import java.net.InetSocketAddress
import java.net.Proxy

fun createProxyForConfig(
    config: ProxyConfig
): Proxy {
    val proxyAddress = InetSocketAddress(config.hostname, config.port)
    return Proxy(config.type, proxyAddress)
}