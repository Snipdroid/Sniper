package ren.imyan.sniper.net.request
import androidx.annotation.Keep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class SubmitAppRequest(
    @SerialName("defaultName")
    val defaultName: String = "",
    @SerialName("localizedName")
    val localizedName: String?,
    @SerialName("languageCode")
    val languageCode: String?,
    @SerialName("packageName")
    val packageName: String?,
    @SerialName("mainActivity")
    val mainActivity: String?,
)