package ren.imyan.sniper.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding by binding(FragmentHomeBinding::bind)

    override fun initView(root: View) {
        super.initView(root)
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                binding?.wallpaper?.setImageDrawable(getWallPaper())
            }
        }.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun loadSingleData() {
        super.loadSingleData()
    }

    @SuppressLint("MissingPermission")
    private fun getWallPaper(): Drawable? {
        var wallpaperDrawable: Drawable? = null
        val wallpaperManager = WallpaperManager.getInstance(this.context)

        wallpaperDrawable = if (wallpaperManager.wallpaperInfo != null) {
            wallpaperManager.wallpaperInfo.loadThumbnail(this.context?.packageManager)
        }else {
            wallpaperManager.drawable
        }

        return wallpaperDrawable
    }
}