package com.example.basictestapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basictestapplication.ui.theme.BasicTestApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            BasicTestApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val data = viewModel.data.collectAsState(initial = emptyList()).value
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenContent(
                        data = data,
                        onSetCacheClick = { scope.launch { viewModel.setDataToCache() } })
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(
    data: List<TodoList?> = emptyList(),
    onSetCacheClick: () -> Unit = {},
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .testTag("MainScreen")
            .fillMaxSize()
    ) {
        Column {
            Text(
                text = "data: $data",
                modifier = Modifier.testTag("DataTextField")
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("CacheButton")
                    .wrapContentHeight(),
                onClick = { onSetCacheClick() }) {
                Text("Set cache value")
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("SampleList"),

                content = {
                    items(50, key = { it }) { index ->
                        ListItem(index = index)
                    }
                    item {
                        FooterItem()
                    }
                }
            )
        }

        val showButton by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomEnd),
            visible = showButton
        ) {
            ScrollToTopButton(
                modifier = Modifier,
                onClick = {
                    scope.launch {
                        // Animate scroll to the first item
                        listState.animateScrollToItem(index = 0)
                    }
                }
            )
        }
    }
}

@Composable
fun ScrollToTopButton(modifier: Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier.testTag("ScrollToTopButton"),
        onClick = onClick
    ) {
        Text("Scroll to top")
    }
}


@Composable
fun ListItem(index: Int) {
    Text(
        text = "${index + 1}",
        modifier = Modifier.testTag("position=$index")
    )
}

@Composable
fun FooterItem() {
    Text(
        text = "Footer",
        modifier = Modifier.testTag("FooterTextField")
    )
}

@PreviewScreenSizes
@PreviewFontScale
@PreviewLightDark
@PreviewDynamicColors
@Composable
fun Preview_main_app() {
    BasicTestApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreenContent()
        }
    }
}
