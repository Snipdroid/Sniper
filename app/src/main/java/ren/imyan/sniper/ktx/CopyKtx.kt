package ren.imyan.sniper.ktx

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun Any.copy(callback: () -> Unit = { Toast.makeText(get(), "复制成功", Toast.LENGTH_SHORT).show() }) {
    val cm: ClipboardManager =
        get<Context>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val data = ClipData.newPlainText("Label", this.toString())
    cm.setPrimaryClip(data)
    callback()
}