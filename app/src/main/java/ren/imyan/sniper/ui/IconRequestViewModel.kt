package ren.imyan.sniper.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.lollipop.iconcore.ui.IconHelper.Companion.parseComponent
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import ren.imyan.sniper.common.setBackground
import ren.imyan.sniper.common.toFile
import ren.imyan.sniper.common.toSize
import ren.imyan.sniper.model.AppInfo
import ren.imyan.sniper.net.AppTrackerApi
import ren.imyan.sniper.net.request.SubmitAppRequest
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

sealed class IconRequestEvent : UiEvent {
    data class UpdateProgress(val progress: Int, val max: Int) : IconRequestEvent()
    object UploadFinish : IconRequestEvent()
    object UploadFail : IconRequestEvent()
}

sealed class IconRequestAction : UiAction {
    object Refresh : IconRequestAction()
    data class RequestApp(val appInfoList: List<AppInfo>) : IconRequestAction()
}

data class IconRequestData(
    val appInfoList: BaseLoad<List<AppInfo>>? = null,
    val iconRequestsData: BaseLoad<IconRequestsData>? = null
) : UiData

data class IconRequestsData(val installed: Int, val themed: Int)

class IconRequestViewModel : BaseViewModel<IconRequestData, IconRequestEvent, IconRequestAction>() {

    private val api = AppTrackerApi
    private val appInfoListCache = mutableListOf<AppInfo>()
    private var progress = 0

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getInstallAppInfo()
        }
    }

    override fun createInitialState(): IconRequestData = IconRequestData()

    override fun dispatch(action: IconRequestAction) {
        when (action) {
            is IconRequestAction.RequestApp -> {
                progress = 0
                requestApp(action.appInfoList)
            }

            IconRequestAction.Refresh -> {
                refreshAppInfo()
            }
        }
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
        val requestedAppList = requestedAppList()
        val tmp = appInfoList.filter { !adaptedList.contains(it.packageName) }
            .filter { it.activityName != "" }
            .map { it.copy(isRequest = it.packageName in requestedAppList) }

        emitData {
            copy(appInfoList = BaseLoad.Success(tmp))
        }

        emitData {
            copy(
                iconRequestsData = BaseLoad.Success(
                    IconRequestsData(
                        installed = appInfoList.filter { it.activityName != "" }.size,
                        themed = adaptedList.intersect(appInfoList.map { it.packageName }
                            .toSet()).size
                    )
                )
            )
        }

        appInfoListCache.clear()
        appInfoListCache.addAll(appInfoList)
    }

    private fun refreshAppInfo() {
        val list = appInfoListCache
        val adaptedList = adaptedFromXml()
        val requestedAppList = requestedAppList()
        val tmp = list.filter { !adaptedList.contains(it.packageName) }
            .filter { it.activityName != "" }
            .map { it.copy(isRequest = it.packageName in requestedAppList) }

        emitData {
            copy(appInfoList = BaseLoad.Success(tmp))
        }
    }

    private fun requestApp(appInfoList: List<AppInfo>) {
        if (appInfoList.isEmpty()) return
        flow {
            val submitAppList = appInfoList.map { appInfo ->
                SubmitAppRequest(
                    activityName = appInfo.activityName,
                    appName = appInfo.appName,
                    packageName = appInfo.packageName
                )
            }
            emit(api.submitAppInfo(submitAppList))
        }.flowOn(Dispatchers.IO).catch { err ->
            err.printStackTrace()
            emitEvent {
                IconRequestEvent.UploadFail
            }
        }.onEach {
            appInfoList.asFlow().onEach { app ->
                app.icon?.let {
                    val iconFile = it.setBackground().toSize(288f, 288f)
                        .toFile(
                            "${app.appName}_${app.packageName}.png",
                            format = Bitmap.CompressFormat.PNG
                        )
                    if (iconFile != null) {
                        delay(200)
                        flow {
                            emit(api.submitAppIcon(app.packageName ?: "", iconFile))
                        }.flowOn(Dispatchers.IO).catch { err ->
                            err.printStackTrace()
                            emitEvent {
                                IconRequestEvent.UploadFail
                            }
                        }.onEach { httpResponse ->
                            if (httpResponse.status == HttpStatusCode.OK) {
                                appRequested(app.packageName ?: "")
                            }
                            emitEvent {
                                IconRequestEvent.UpdateProgress(progress++, appInfoList.size)
                            }
                            if (appInfoList.size == progress) {
                                emitEvent {
                                    IconRequestEvent.UploadFinish
                                }
                            }
                        }.launchIn(viewModelScope)
                    }
                }
            }.launchIn(viewModelScope)
        }.launchIn(viewModelScope)
    }

    /**
     * 获取已经请求过的应用
     *
     * @return 已经请求的应用的 packageName 列表
     */
    private fun requestedAppList(): List<String> {
        val filterPackageList = ArrayList<String>()

        val file = File(get<Context>().cacheDir, "requested.txt")

        if (!file.exists()) {
            file.createNewFile()
        }

        BufferedReader(FileReader(file)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                filterPackageList.add(line)
                line = reader.readLine()
            }
        }

        return filterPackageList.distinct()
    }

    private fun appRequested(packageName: String) {
        val file = File(get<Context>().cacheDir, "requested.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        BufferedWriter(FileWriter(file, true)).use { writer ->
            writer.append(packageName)
            writer.newLine()
        }
    }

    /**
     * 从 XML 里面获取已经适配的应用
     *
     * @return 适配应用的 packageName 列表
     */
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