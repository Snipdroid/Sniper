package ren.imyan.sniper.model

import android.graphics.Bitmap

data class AppInfo(
    val appName: String?,
    val packageName: String?,
    val activityName: String?,
    val icon: Bitmap?,
    var isRequest: Boolean = false,
    var isCheck: Boolean = false
)
