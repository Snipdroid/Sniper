package ren.imyan.sniper.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import ren.imyan.sniper.Store
import ren.imyan.sniper.entity.AppInfo
import ren.imyan.sniper.ktx.copy
import ren.imyan.sniper.ktx.get

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RequestScreen() {
    val appInfoList = get<Store>().appInfoList.collectAsState()

    val selectedAppList = remember {
        mutableStateListOf<AppInfo>()
    }

    val openSendDialog = rememberSaveable { mutableStateOf(false) }

    SendDialog(state = openSendDialog, appInfoList = selectedAppList)

    // 保存滚动状态
    val listState = rememberLazyListState()

    Scaffold(floatingActionButton = {
        AnimatedVisibility(
            visible = selectedAppList.size > 0,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            FloatingActionButton(onClick = {
                openSendDialog.value = true
            }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_media_play),
                    contentDescription = null
                )
            }
        }
    }) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            items(
                count = appInfoList.value.size,
                itemContent = { index ->
                    AppItem(appInfoList.value[index], selectedAppList)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(appInfo: AppInfo, selectedAppList: MutableList<AppInfo>) {
    var isCheck by rememberSaveable { mutableStateOf(false) }
    val openCopyDialog = remember { mutableStateOf(false) }

    CopyDialog(state = openCopyDialog, appInfo = appInfo)

    if (isCheck) {
        selectedAppList.add(appInfo)
    } else {
        selectedAppList.remove(appInfo)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    isCheck = !isCheck
                },
                onLongClick = {
                    openCopyDialog.value = true
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp, 16.dp)
        ) {
            AsyncImage(
                modifier = Modifier.size(50.dp),
                model = appInfo.icon,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    text = appInfo.appName ?: "App Name"
                )
                Text(
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = Color(0xff6d6d6d),
                    text = appInfo.packageName ?: "App Package Name"
                )
            }
        }
        Checkbox(checked = isCheck, onCheckedChange = {
            isCheck = it
        })
    }
}

private data class DialogItem(
    val name: String,
    val onClick: () -> Unit,
    val isHighlight: Boolean? = false,
)

@Composable
fun SendDialog(state: MutableState<Boolean>, appInfoList: List<AppInfo>) {
    if (!state.value) return

    val sendItemList = listOf(
        DialogItem(
            name = "复制 APP 名称和包名",
            onClick = {
                val stringBuilder = StringBuilder().apply {
                    appInfoList.forEach {
                        append(
                            """
                            应用名：${it.appName}
                            包名：${it.packageName}
                        """.trimIndent()
                        )
                        append("\n")
                    }
                }
                stringBuilder.toString().copy()
            }
        ),
        DialogItem(
            name = "复制 appfilter",
            onClick = {
                val stringBuilder = StringBuilder().apply {
                    append("<resources>")
                    appInfoList.forEach {
                        append("\n")
                        append("<!-- ${it.appName} -->\n")
                        append("<item component=\"ComponentInfo{${it.packageName}/${it.activityName}}\" drawable=\"${it.appName}\"/>\n")
                        append("\n")
                    }
                    append("</resources>")
                }
                stringBuilder.toString().copy()
            }
        ),
        DialogItem(
            name = "打包 ZIP 然后分享",
            isHighlight = true,
            onClick = {

            }
        ),
        DialogItem(
            name = "上传到服务器",
            isHighlight = true,
            onClick = {
                val appIconMap = mutableMapOf<String, Bitmap>()
                appInfoList.forEach {
                    if (it.packageName != null && it.icon != null) {
                        appIconMap[it.packageName] = it.icon
                    }
                }
                get<Store>().submitAll(appInfoList, appIconMap)
            }
        ),
    )

    Dialog(onDismissRequest = { state.value = false }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                sendItemList.forEach {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                it.onClick()
                            }
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ), contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = it.name)
                    }
                }
            }
        }
    }
}

@Composable
fun CopyDialog(state: MutableState<Boolean>, appInfo: AppInfo) {

    val copyItemList = listOf(
        DialogItem(
            name = "复制包名",
            onClick = {
                appInfo.packageName?.copy()
            }
        ),
        DialogItem(
            name = "复制启动项",
            onClick = {
                appInfo.activityName?.copy()
            }
        ),
        DialogItem(
            name = "复制应用名 + 包名",
            onClick = {
                """
应用名：${appInfo.appName}
包名：${appInfo.packageName}
                                           """.trimIndent().copy()
            }
        ),
        DialogItem(
            name = "复制 appfilter",
            onClick = {
                """
<item component="ComponentInfo{${appInfo.packageName}/${appInfo.activityName}}" drawable="${appInfo.appName}"/>
                                            """.trimIndent().copy()
            }
        ),
    )

    if (state.value) {
        Dialog(onDismissRequest = { state.value = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    copyItemList.forEach {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    it.onClick()
                                }
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ), contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = it.name)
                        }
                    }
                }
            }
        }
    }
}
