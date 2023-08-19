package ren.imyan.sniper.ui.request

import android.app.Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ren.imyan.sniper.R
import ren.imyan.sniper.databinding.DialogRequestBinding

class RequestDialog(activity: Activity) {
    private val binding = DialogRequestBinding.inflate(activity.layoutInflater, null, false)
    private val dialog =
        MaterialAlertDialogBuilder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setView(binding.root).setTitle(R.string.uploading)
            .setIcon(R.drawable.outline_cloud_upload_24)
            .setCancelable(false)
            .setPositiveButton(R.string.cancel) { _, _ -> }.create()

    fun show() {
        dialog.show()
        binding.apply {
            loading.isIndeterminate = true
        }
    }

    fun updateProgress(progress: Int, max: Int) {
        binding.loading.apply {
            setMax(max)
            setProgressCompat(progress, true)
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }
}