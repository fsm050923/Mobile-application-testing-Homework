package com.example.basictestapplication

import androidx.lifecycle.ViewModel
import com.example.basictestapplication.glance.GlanceDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cache: LocalCacheSampleImpl,
    private val glanceDataProvider: GlanceDataProvider,
) : ViewModel() {

    suspend fun setDataToCache() {
        val data = Random.nextInt()
        val newValue = listOf(TodoList(data.toString()))
        cache.setTodos(newValue)
        glanceDataProvider.saveText(data.toString())
    }

    val data = cache.cache

    suspend fun getData() = cache.getTodosFlow()

    fun getData2(): List<TodoList?> {
        return cache.getTodos()
    }
}