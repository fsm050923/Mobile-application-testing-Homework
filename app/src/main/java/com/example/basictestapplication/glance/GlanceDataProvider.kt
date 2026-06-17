package com.example.basictestapplication.glance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class GlanceDataProvider @Inject constructor(
    @ApplicationContext val context: Context,
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { preferences ->
                if (preferences[glance_text_key].isNullOrEmpty()) {
                    preferences[glance_text_key] = "Sample text"
                }
            }
        }
    }

    val glance_text_key = stringPreferencesKey("glance_text_key")
    suspend fun saveText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[glance_text_key] = text
        }
        updateWidget()
    }

    private suspend fun updateWidget() {
        val manager = GlanceAppWidgetManager(context)
        val widget = GlanceDataStoreWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }

    val glanceTextFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[glance_text_key] ?: "empty"
        }
}
