package ren.imyan.sniper.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val netModule = module {
    factory {
        HttpClient(Android) {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "apptracker-dev.cn2.tiers.top"
                }
                header("Content-Type", "application/json")
            }
            install(ContentNegotiation) {
                json()
            }
            install(NetLoggingPlugin)
        }
    }
}