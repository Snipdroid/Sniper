package ren.imyan.sniper.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private val binding by binding(FragmentSettingsBinding::bind)

    override fun initView(root: View) {
        super.initView(root)
        val settingsPreferences = SettingsPreferences()
        childFragmentManager.beginTransaction().apply {
            binding?.settingsFragment?.let { replace(it.id, settingsPreferences) }
            commit()
        }
    }
}

class SettingsPreferences : PreferenceFragmentCompat() {

    private val licenseList = listOf(
        License(
            name = "Koin",
            summary = "https://github.com/InsertKoinIO/koin",
            link = "https://github.com/InsertKoinIO/koin",
            icon = R.drawable.baseline_open_in_new_24
        ),
        License(
            name = "Coil",
            summary = "https://github.com/coil-kt/coil",
            link = "https://github.com/coil-kt/coil",
            icon = R.drawable.baseline_open_in_new_24
        ),
        License(
            name = "Timber",
            summary = "https://github.com/JakeWharton/timber",
            link = "https://github.com/JakeWharton/timber",
            icon = R.drawable.baseline_open_in_new_24
        ),
        License(
            name = "Ktor",
            summary = "https://github.com/ktorio/ktor",
            link = "https://github.com/ktorio/ktor",
            icon = R.drawable.baseline_open_in_new_24
        ),
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val licenseCategory = findPreference<PreferenceCategory>("license")

        licenseList.forEach { license ->
            val licensePreference = Preference(requireContext()).apply {
                title = license.name
                summary = license.summary
                icon = ResourcesCompat.getDrawable(resources, license.icon, null)

                setOnPreferenceClickListener {
                    openWebPage(license.link)
                    true
                }
            }

            licenseCategory?.addPreference(licensePreference)
        }
    }

    private fun openWebPage(url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}

data class License(
    val name: String,
    val summary: String,
    val link: String,
    @DrawableRes val icon: Int
)

