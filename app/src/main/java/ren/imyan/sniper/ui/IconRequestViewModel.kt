package ren.imyan.sniper.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.lollipop.iconcore.ui.IconHelper.Companion.parseComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.base.BaseViewModel
import ren.imyan.sniper.base.UiAction
import ren.imyan.sniper.base.UiData
import ren.imyan.sniper.base.UiEvent
import ren.imyan.sniper.common.activityName
import ren.imyan.sniper.common.get
import ren.imyan.sniper.common.originalIcon
import ren.imyan.sniper.model.AppInfo

sealed class IconRequestEvent : UiEvent

sealed class IconRequestAction : UiAction

data class IconRequestData(val appInfoList: BaseLoad<List<AppInfo>>? = null) : UiData

class IconRequestViewModel : BaseViewModel<IconRequestData, IconRequestEvent, IconRequestAction>() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getInstallAppInfo()
        }
    }

    override fun createInitialState(): IconRequestData = IconRequestData()

    override fun dispatch(action: IconRequestAction) {
        TODO("Not yet implemented")
    }

    private fun getInstallAppInfo() {
        emitData {
            copy(appInfoList = BaseLoad.Loading)
        }

        val packages = get<Context>().packageManager.getInstalledPackages(0)
        val appInfoList = mutableListOf<AppInfo>()

        packages.forEach {
            appInfoList.add(
                AppInfo(
                    appName = it.applicationInfo.loadLabel(get<Context>().packageManager)
                        .toString(),
                    packageName = it.packageName,
                    activityName = it.activityName,
                    icon = it.originalIcon
                )
            )
        }

        appInfoList.sortBy {
            it.appName
        }

        val adaptedList = adaptedFromXml()
        val tmp = appInfoList.filter { !adaptedList.contains(it.packageName) }
            .filter { it.activityName != "" }

        emitData {
            copy(appInfoList = BaseLoad.Success(tmp))
        }
    }

    private fun adaptedFromXml(): ArrayList<String> {
        val filterPackageList = ArrayList<String>()
        val xml = get<Context>().resources.getXml(R.xml.appfilter)
        var eventType = xml.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = xml.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if ("item" == tagName) {
                        val component =
                            parseComponent(xml.getAttributeValue(null, "component") ?: "")
                        filterPackageList.add(component.packageName)
                    }
                }
            }
            eventType = xml.next()
        }
        return filterPackageList
    }
}