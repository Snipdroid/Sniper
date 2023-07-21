package ren.imyan.sniper.common

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.color.MaterialColors

@ColorInt
fun Context.getThemeAttrColor(
    @StyleRes themeResId: Int, @AttrRes attrResId: Int
): Int {
    return MaterialColors.getColor(
        ContextThemeWrapper(this, themeResId), attrResId, Color.WHITE
    )
}