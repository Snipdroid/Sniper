package ren.imyan.sniper.common

import android.content.Context
import android.util.DisplayMetrics

fun Int.pxToDp(context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return (this / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

fun Float.pxToDp(context: Context): Float {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return this / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}