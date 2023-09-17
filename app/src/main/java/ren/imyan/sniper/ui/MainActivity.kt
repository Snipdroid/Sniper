package ren.imyan.sniper.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ren.imyan.sniper.R
import ren.imyan.sniper.common.DataStoreUtil
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.getThemeAttrColor
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
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        val uiMode = resources.configuration.uiMode
        if ((uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            setTransparentStyle(binding.root, window, false)
        } else {
            setTransparentStyle(binding.root, window)
        }
        setContentView(binding.root)

        val color = this.getThemeAttrColor(
            R.style.Theme_Sniper, com.google.android.material.R.attr.colorSurfaceContainer
        )

        window.navigationBarColor = color

        binding.pager.apply {
            offscreenPageLimit = fragmentClazzList.size
            isUserInputEnabled = false
            adapter = MainFragmentAdapter(this@MainActivity)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.bottomNavigation.selectedItemId =
                        binding.bottomNavigation.menu[position].itemId
                }
            })
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    binding.pager.setCurrentItem(0, false)
                }

                R.id.icons -> {
                    binding.pager.setCurrentItem(1, false)
                }

                R.id.request -> {
                    binding.pager.setCurrentItem(2, false)
                }

                R.id.settings -> {
                    binding.pager.setCurrentItem(3, false)
                }

                else -> {
                    binding.pager.setCurrentItem(0, false)
                }
            }
            true
        }

        lifecycleScope.launch {
            DataStoreUtil.getData("privacy", false).collectLatest { isPrivacy ->
                if (!isPrivacy) {
                    PrivacyDialog(this@MainActivity).show()
                }
            }
        }
    }


    inner class MainFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = fragmentClazzList.size

        override fun createFragment(position: Int): Fragment =
            fragmentClazzList[position].newInstance()
    }
}