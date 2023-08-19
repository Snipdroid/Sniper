package ren.imyan.sniper.net

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.util.cio.readChannel
import ren.imyan.sniper.common.get
import ren.imyan.sniper.net.request.SubmitAppRequest
import java.io.File

object AppTrackerApi {
    private val client = get<HttpClient>()

    suspend fun submitAppInfo(appInfoList: List<SubmitAppRequest>) = client.post("/api/appinfo") {
        setBody(appInfoList)
    }

    suspend fun submitAppIcon(packageName: String, icon: File) = client.post("/api/icon") {
        headers {
            remove("Content-Type")
            append("Content-Type", "image/png")
        }
        url {
            parameter("packageName", packageName)
        }
        setBody(icon.readChannel())
    }
}