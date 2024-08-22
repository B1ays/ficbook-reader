package ru.blays.ficbook.reader.shared.proxy

import ru.blays.ficbook.reader.SharedBuildKonfig
import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import java.net.Proxy

internal val defaultProxyConfig = ProxyConfig(
    hostname = SharedBuildKonfig.proxyHost,
    port = SharedBuildKonfig.proxyPort,
    type = Proxy.Type.valueOf(SharedBuildKonfig.proxyType),
    username = SharedBuildKonfig.proxyUsername,
    password = SharedBuildKonfig.proxyPassword
)