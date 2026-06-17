package com.example.basictestapplication.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.basictestapplication.glance.GlanceDataProvider
import com.example.basictestapplication.glance.GlanceDataStoreWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


@Singleton
class WidgetWorkerAdapter @Inject constructor(
    private val workManager: WorkManager,
) {
    fun startWidgetAppWorker() {
        workManager.enqueueUniquePeriodicWork(
            "start-widget-updater-work",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            widgetUpdateRequest
        )
    }

    private val widgetUpdateRequest =
        PeriodicWorkRequestBuilder<WidgetWorker>(
            repeatInterval = 15L,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).build()
}

@HiltWorker
class WidgetWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val glanceDataProvider: GlanceDataProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val newValue = Random.nextInt().toString()
        Timber.d("Updating widget $newValue")
        glanceDataProvider.saveText(newValue)
        GlanceDataStoreWidget().updateAll(context)
        Timber.d("Updated widget")
        return Result.success()
    }
}
