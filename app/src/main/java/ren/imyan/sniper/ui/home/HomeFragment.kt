package ren.imyan.sniper.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.observeState
import ren.imyan.sniper.databinding.FragmentHomeBinding
import ren.imyan.sniper.ui.IconPackData
import ren.imyan.sniper.ui.IconPackViewModel
import ren.imyan.sniper.ui.IconRequestData
import ren.imyan.sniper.ui.IconRequestViewModel

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding by binding(FragmentHomeBinding::bind)
    private val iconPackViewModel by activityViewModels<IconPackViewModel>()
    private val requestViewModel by activityViewModels<IconRequestViewModel>()

    override fun initView(root: View) {
        super.initView(root)
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                binding?.wallpaper?.setImageDrawable(getWallPaper())
            }
        }.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun initViewModel(viewLifecycleOwner: LifecycleOwner) {
        super.initViewModel(viewLifecycleOwner)
        iconPackViewModel.uiData.observeState(viewLifecycleOwner, IconPackData::iconCount) {
            when (it) {
                is BaseLoad.Success -> {
                    binding?.apply {
                        iconCount.text = it.data.toString()
                    }
                }

                else -> {}
            }
        }

        requestViewModel.uiData.observeState(
            viewLifecycleOwner,
            IconRequestData::iconRequestsData
        ) {
            when (it) {
                is BaseLoad.Success -> {
                    binding?.apply {
                        installCount.text =
                            String.format(getString(R.string.install_app), it.data.installed)
                        themedCount.text =
                            String.format(getString(R.string.themed_app), it.data.themed)
                        missedCount.text = String.format(
                            getString(R.string.missed_app),
                            it.data.installed - it.data.themed
                        )
                        themedProcess.apply {
                            max = it.data.installed
                            progress = it.data.themed
                        }
                    }
                }

                else -> {

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getWallPaper(): Drawable? {
        var wallpaperDrawable: Drawable? = null
        val wallpaperManager = WallpaperManager.getInstance(this.context)

        wallpaperDrawable = if (wallpaperManager.wallpaperInfo != null) {
            wallpaperManager.wallpaperInfo.loadThumbnail(this.context?.packageManager)
        } else {
            wallpaperManager.drawable
        }

        return wallpaperDrawable
    }
}