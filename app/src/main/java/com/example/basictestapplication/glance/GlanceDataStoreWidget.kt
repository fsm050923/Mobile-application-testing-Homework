package com.example.basictestapplication.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.example.basictestapplication.MainActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class GlanceDataStoreWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(50.dp, 50.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SQUARE,
            HORIZONTAL_RECTANGLE,
            BIG_SQUARE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }


    @Composable
    private fun MyContent() {
        val size = LocalSize.current
        val context = LocalContext.current
        val dataStore = GlanceDataProvider(context)
        val glanceText = runBlocking { dataStore.glanceTextFlow.firstOrNull() }

//               var glanceText by rememberSaveable { mutableStateOf("") }
//        LaunchedEffect(true) {
//            dataStore.glanceTextFlow.firstOrNull()?.let {
//                glanceText = it
//            }
//        }


        Column(
            modifier = GlanceModifier.fillMaxSize().background(Color.Cyan).clickable(
                onClick = actionStartActivity<MainActivity>()
            ).padding(12.dp)
        ) {
            if (size.height >= BIG_SQUARE.height) {
                Text(text = "Widget is very tall!")
            }

            if (size.width >= HORIZONTAL_RECTANGLE.width) {
                Text("Widget is very wide!")
            }
            if (size.width >= SMALL_SQUARE.width && size.height >= SMALL_SQUARE.height) {
                Text("Widget covers at least this small circle :) Cached value: $glanceText")
            }
        }
    }
}