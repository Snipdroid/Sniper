package ren.imyan.sniper

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.DisplayMetrics
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import ren.imyan.sniper.common.DrawableParser
import ren.imyan.sniper.common.LocalUtils
import ren.imyan.sniper.entity.AppInfo
import ren.imyan.sniper.entity.IconInfo
import ren.imyan.sniper.ktx.*
import ren.imyan.sniper.net.AppTrackerApi
import ren.imyan.sniper.net.request.SubmitAppRequest

class Store : ViewModel() {

    private val api = AppTrackerApi

    private val _iconList = MutableStateFlow(listOf<IconInfo>())

    val iconList = _iconList.asStateFlow()
    val iconPaging = Pager(PagingConfig(pageSize = 20), pagingSourceFactory = {
        FlowPagingSource(iconList)
    }).flow.cachedIn(viewModelScope)

    private val _iconListWithCate = MutableStateFlow(mapOf<String?, List<IconInfo>>())
    val iconListWithCate = _iconListWithCate.asStateFlow()

    private val _appInfoList = MutableStateFlow(listOf<AppInfo>())
    val appInfoList = _appInfoList.asStateFlow()

    private val appInfoListSource = mutableListOf<AppInfo>()
    private val currAppInfoList = mutableListOf<AppInfo>()

    // 添加上传状态管理
    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val uploadStatus = _uploadStatus.asStateFlow()

    init {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                DrawableParser.getDrawable()
            }
            _iconList.value = data
            _iconListWithCate.value = data.groupBy { it.category }

            getAppInfoList()
        }
    }

    /**
     * 获取所有应用信息
     */
    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getAppInfoList() {
        withContext(Dispatchers.IO) {
            val packages = get<Context>().packageManager.getInstalledPackages(0)
            val appInfos = mutableListOf<AppInfo>()

            packages.forEach {
                val appName =
                    it.applicationInfo?.loadLabel(get<Context>().packageManager).toString()
                val packageName = it.packageName
                val activityName = activityName(it)
                val icon = it.getOriginalIcon()
                val isSystem = it.applicationInfo?.flags!! and 1 != 0
                appInfos.add(AppInfo(appName, packageName, activityName, icon, isSystem))
            }

            appInfos.sortBy { it.appName }
            appInfoListSource.clear()
            appInfoListSource.addAll(appInfos)
            currAppInfoList.clear()
            currAppInfoList.addAll(appInfos.filter { it.activityName != "" })
            _appInfoList.emit(appInfos.filter { it.activityName != "" })
        }
    }

    /**
     * 获取启动 Activity name
     */
    private fun activityName(pi: PackageInfo): String {
        val resolveIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(pi.packageName)
        }
        val resolveInfoList = get<Context>().packageManager.queryIntentActivities(resolveIntent, 0)
        kotlin.runCatching {
            val resolveInfo = resolveInfoList.iterator().next()
            resolveInfo?.let {
                return it.activityInfo.name
            }
        }
        return ""
    }

    /**
     * 从 appfilter 中获取过滤的应用包名
     */
    private fun filterFromXml(): ArrayList<String> {
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

    private fun parseComponent(info: String): ComponentName {
        try {
            if (!info.startsWith(KEY_COMPONENT_INFO)) {
                return EMPTY_COMPONENT
            }
            val start = info.indexOf("{") + 1
            val end = info.indexOf("}")
            if (start > end || start == end) {
                return EMPTY_COMPONENT
            }
            val infoContent = info.substring(start, end)
            val split = infoContent.split("/")
            if (split.size < 2) {
                return EMPTY_COMPONENT
            }
            val pkg = split[0]
            val cls = split[1]
            val fullName = if (cls[0] == '.') {
                pkg + cls
            } else {
                cls
            }
            return ComponentName(pkg, fullName)
        } catch (e: Throwable) {
            return EMPTY_COMPONENT
        }
    }

    /**
     * 上传应用信息 + 应用图标
     * 使用Flow异步处理上传流程
     */
    fun submitAll(appInfoList: List<AppInfo>, iconList: Map<String, Bitmap>) {
        // 步骤1: 构造应用信息并上传
        flow {
            _uploadStatus.value = UploadStatus.SubmittingAppInfo

            val submitAppInfoList = appInfoList.map { appInfo ->
                SubmitAppRequest(
                    languageCode = LocalUtils.getCurrentLocale(get()).toLanguageTag(),
                    packageName = appInfo.packageName,
                    mainActivity = appInfo.activityName,
                    localizedName = appInfo.appName
                )
            }

            // 提交应用信息并将结果发射
            emit(api.submitAppInfo(submitAppInfoList))
        }
            .flowOn(Dispatchers.IO)
            .onStart {
                // 准备上传
            }
            .catch { error ->
                // 应用信息上传失败
                error.printStackTrace()
                _uploadStatus.value = UploadStatus.Error("应用信息上传失败: ${error.message}")
            }
            .onEach { _ ->
                // 应用信息上传成功，开始上传图标
                _uploadStatus.value = UploadStatus.SubmittingIcons(0, iconList.size)
                submitIcons(iconList).launchIn(viewModelScope)
            }
            .launchIn(viewModelScope)
    }

    /**
     * 上传应用图标
     * @param iconList 图标列表，键为包名，值为图标位图
     * @return 上传图标的Flow
     */
    private fun submitIcons(iconList: Map<String, Bitmap>): Flow<Unit> = flow {
        iconList.forEach { (packageName, bitmap) ->
            val iconFile = bitmap.setBackground().toSize(288f, 288f)
                .toFile("${packageName}.png", format = Bitmap.CompressFormat.PNG)

            if (iconFile != null) {
                try {
                    val submitResponse = api.submitAppIcon(packageName)
                    if (submitResponse.status == HttpStatusCode.OK) {
                        submitResponse

                        val uploadUrl: String = submitResponse.body()
                        api.uploadToS3(uploadUrl, iconFile)
                    }

                    val currentValue = _uploadStatus.value
                    if (currentValue is UploadStatus.SubmittingIcons) {
                        _uploadStatus.value = UploadStatus.SubmittingIcons(
                            currentValue.current + 1,
                            currentValue.total
                        )
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }

        val currentValue = _uploadStatus.value
        if (currentValue is UploadStatus.SubmittingIcons) {
            _uploadStatus.value = UploadStatus.Completed(currentValue.current)
        }
        emit(Unit)
    }.flowOn(Dispatchers.IO) // 使用 flowOn 来指定上游flow的执行线程

    // 上传状态密封类
    sealed class UploadStatus {
        object Idle : UploadStatus()
        object SubmittingAppInfo : UploadStatus()
        data class SubmittingIcons(val current: Int, val total: Int) : UploadStatus()
        data class Completed(val successCount: Int) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }

    companion object {
        private val EMPTY_COMPONENT = ComponentName("", "")
        private const val KEY_COMPONENT_INFO = "ComponentInfo"
    }
}

fun PackageInfo.getOriginalIcon(): Bitmap? {
    val trueContext =
        get<Context>().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)
    try {
        val displayMetrics = arrayOf(
            DisplayMetrics.DENSITY_XXXHIGH,
            DisplayMetrics.DENSITY_XXHIGH,
            DisplayMetrics.DENSITY_XHIGH,
            DisplayMetrics.DENSITY_HIGH,
            DisplayMetrics.DENSITY_TV
        )

        for (ele in displayMetrics) {
            try {
                val icon = trueContext.resources.getDrawableForDensity(
                    this.applicationInfo!!.icon,
                    ele,
                    get<Context>().theme
                )
                if (icon != null) {
                    return icon.toBitmap()
                }
            } catch (e: Resources.NotFoundException) {
                continue
            }
        }
    } catch (e: Exception) {

    }

    var icon: Bitmap? = null

    kotlin.runCatching {
        icon = this.applicationInfo!!.loadLogo(trueContext.packageManager).toBitmap()
    }

    return icon
}
