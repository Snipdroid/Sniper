package ren.imyan.sniper.ui

import android.app.Activity
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import ren.imyan.sniper.R
import ren.imyan.sniper.common.DataStoreUtil
import ren.imyan.sniper.common.appGlobalScope
import ren.imyan.sniper.databinding.DialogPrivacyBinding
import kotlin.system.exitProcess

class PrivacyDialog(private val activity: Activity) {
    private val binding = DialogPrivacyBinding.inflate(activity.layoutInflater, null, false)
    private val dialog =
        MaterialAlertDialogBuilder(
            activity
        ).setView(binding.root).setTitle(activity.getString(R.string.privacy_title))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.agree)) { _, _ ->
                appGlobalScope.launch {
                    DataStoreUtil.putData("privacy", true)
                }
            }.setNegativeButton(activity.getString(R.string.disagree)) { _, _ ->
                activity.finish()
                exitProcess(0)
            }.create()

    fun show() {
        binding.text.apply {
            val markwon = Markwon.builder(activity).build()
            val str = getStringFromRaw(activity, R.raw.privacy)
            markwon.setMarkdown(this, str)
        }
        dialog.show()
    }

    private
    fun getStringFromRaw(context: Context, fileId: Int): String {
        val inputStream = context.resources.openRawResource(fileId)
        return inputStream.bufferedReader().use { it.readText() }
    }
}