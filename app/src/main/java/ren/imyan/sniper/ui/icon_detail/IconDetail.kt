@file:OptIn(ExperimentalMaterial3Api::class)

package ren.imyan.sniper.ui.icon_detail

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ren.imyan.sniper.Store
import ren.imyan.sniper.ktx.get
import ren.imyan.sniper.ui.home.stringToId

@Composable
fun IconDetail(category: String?) {
    val iconListWithCate by get<Store>().iconListWithCate.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var previewDrawableId by rememberSaveable { mutableIntStateOf(0) }

    // 缓存分类后的图标列表
    val filteredIcons = remember(iconListWithCate, category) {
        iconListWithCate[category]?.flatMap { it.items } ?: emptyList()
    }

    // 保存滚动状态
    val gridState = rememberLazyGridState()

    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredIcons, key = { it.drawable }) { icon ->
                AsyncImage(
                    modifier = Modifier.clickable {
                        previewDrawableId = stringToId(icon.drawable)
                        showBottomSheet = true
                    },
                    model = stringToId(icon.drawable),
                    contentDescription = icon.drawable
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                if (previewDrawableId != 0) {
                    AsyncImage(model = previewDrawableId, contentDescription = "")
                }
            }
        }
    }
}