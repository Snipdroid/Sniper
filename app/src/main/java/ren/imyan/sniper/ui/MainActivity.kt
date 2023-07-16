package ren.imyan.sniper.ui

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.color.MaterialColors
import ren.imyan.sniper.R
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.setTransparentStyle
import ren.imyan.sniper.databinding.ActivityMainBinding
import ren.imyan.sniper.ui.home.HomeFragment
import ren.imyan.sniper.ui.icons.IconsFragment
import ren.imyan.sniper.ui.request.RequestFragment
import ren.imyan.sniper.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private val binding by binding(ActivityMainBinding::inflate)

    private val fragmentClazzList by lazy {
        listOf(
            HomeFragment::class.java,
            IconsFragment::class.java,
            RequestFragment::class.java,
            SettingsFragment::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentStyle(binding.root, window)
        setContentView(binding.root)

        window.navigationBarColor = getThemeAttrColor(this,R.style.Theme_Sniper ,com.google.android.material.R.attr.colorSurface)

        binding.pager.apply {
            offscreenPageLimit = fragmentClazzList.size
            adapter = MainFragmentAdapter(this@MainActivity)
            isUserInputEnabled = false
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    binding.pager.currentItem = 0
                }

                R.id.icons -> {
                    binding.pager.currentItem = 1
                }

                R.id.request -> {
                    binding.pager.currentItem = 2
                }

                R.id.settings -> {
                    binding.pager.currentItem = 3
                }

                else -> {
                    binding.pager.currentItem = 0
                }
            }
            true
        }
    }

    @ColorInt
    fun getThemeAttrColor(@NonNull context: Context, @StyleRes themeResId: Int, @AttrRes attrResId: Int): Int {
        return MaterialColors.getColor(ContextThemeWrapper(context, themeResId), attrResId, Color.WHITE)
    }


    inner class MainFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = fragmentClazzList.size

        override fun createFragment(position: Int): Fragment =
            fragmentClazzList[position].newInstance()
    }
}