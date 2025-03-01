package ren.imyan.sniper.entity

import android.graphics.Bitmap

data class AppInfo(
    val appName: String?,
    val packageName: String?,
    val activityName: String?,
    val icon: Bitmap?,
    val isSystem: Boolean?,
    var isCheck: Boolean = false
)
