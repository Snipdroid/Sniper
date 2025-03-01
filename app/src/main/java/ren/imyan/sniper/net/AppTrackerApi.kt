package ren.imyan.sniper.net

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.util.cio.*
import ren.imyan.sniper.ktx.get
import ren.imyan.sniper.net.request.SubmitAppRequest
import java.io.File

object AppTrackerApi {
    private val client = get<HttpClient>()
    suspend fun submitAppInfo(appInfoList: List<SubmitAppRequest>) = client.post("/app-info/create") {
        setBody(appInfoList)
    }

    suspend fun submitAppIcon(packageName: String) =
        client.get("/app-icon/generate-upload-url") {
            url {
                parameter("packageName", packageName)
            }
        }

    suspend fun uploadToS3(url: String, icon: File) = client.put(url) {
        headers {
            remove("Content-Type")
            append("Content-Length", icon.length().toString())
            append("Content-Type", "image/png")
        }
        setBody(icon.readChannel())
    }
}
