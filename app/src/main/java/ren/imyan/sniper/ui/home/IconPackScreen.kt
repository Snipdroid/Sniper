package ren.imyan.sniper.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ren.imyan.sniper.LocalNavController
import ren.imyan.sniper.Store
import ren.imyan.sniper.entity.IconInfo
import ren.imyan.sniper.ktx.get

@Composable
fun IconPackScreen() {
    val iconList by get<Store>().iconList.collectAsState()

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize().safeDrawingPadding()
    ) {
        items(iconList) { iconInfo ->
            Item(iconInfo)
        }
    }
}

@Composable
fun Item(data: IconInfo?) {
    val nav = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 16.dp)
            .clickable {
                nav.navigate("icon_detail/${data?.category}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = data?.category ?: "", fontSize = 20.sp)
            Text(text = "${data?.items?.size.toString()} Icons", fontSize = 20.sp)
        }
        Row {
            data?.items?.take(4)?.forEach {
                AsyncImage(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 20.dp),
                    model = stringToId(it.drawable),
                    contentDescription = it.drawable
                )
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
fun stringToId(name: String): Int {
    return get<Context>().resources.getIdentifier(name, "drawable", get<Context>().packageName)
}
