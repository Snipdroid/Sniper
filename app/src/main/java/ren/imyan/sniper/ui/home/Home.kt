package ren.imyan.sniper.ui.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun Home() {
    val items = listOf(
        Screen.IconPackScreen,
        Screen.RequestScreen,
    )
    val scope = rememberCoroutineScope()
    val pageState = rememberPagerState() {
        items.size
    }

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            items.forEach {
                item(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text(it.label) },
                    selected = items.indexOf(it) == pageState.currentPage,
                    onClick = {
                        scope.launch {
                            pageState.scrollToPage(items.indexOf(it))
                        }
                    }
                )
            }
        },
    ) {
        HorizontalPager(
            userScrollEnabled = false,
            state = pageState,
        ) { index -> items[index].content() }
    }
}

