package ren.imyan.sniper.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.DisplayMetrics
import androidx.core.graphics.drawable.toBitmap

val PackageInfo.activityName: String
    get() {
        val resolveIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(this@activityName.packageName)
        }
        val resolveInfoList = get<Context>().packageManager.queryIntentActivities(resolveIntent, 0)

        kotlin.runCatching {
            val resolveInfo = resolveInfoList.iterator().next()
            resolveInfo?.let {
                return it.activityInfo.name
            }
        }
        return ""
    }

val PackageInfo.originalIcon: Bitmap?
    get() {
        val trueContext =
            get<Context>().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)
        try {
            val displayMetrics = arrayOf(
                DisplayMetrics.DENSITY_XXXHIGH,
                DisplayMetrics.DENSITY_XXHIGH,
                DisplayMetrics.DENSITY_XHIGH,
                DisplayMetrics.DENSITY_HIGH,
                DisplayMetrics.DENSITY_TV
            )

            for (ele in displayMetrics) {
                try {
                    val icon = trueContext.resources.getDrawableForDensity(
                        this.applicationInfo.icon,
                        ele,
                        get<Context>().theme
                    )
                    if (icon != null) {
                        return icon.toBitmap()
                    }
                } catch (e: Resources.NotFoundException) {
                    continue
                }
            }
        } catch (e: Exception) {

        }

        var icon: Bitmap? = null

        kotlin.runCatching {
            icon = this.applicationInfo.loadLogo(trueContext.packageManager).toBitmap()
        }

        return icon
    }