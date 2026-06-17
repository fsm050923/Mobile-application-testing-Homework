package com.example.basictestapplication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocalCacheSampleImpl @Inject constructor() : LocalCacheSample {

    private val mCache: MutableStateFlow<List<TodoList?>> = MutableStateFlow(emptyList())
    val cache = mCache.asStateFlow()

    override fun getTodos(): List<TodoList?> = mCache.value

    override suspend fun getTodosFlow(): StateFlow<List<TodoList?>> = mCache

    override fun setTodos(new: List<TodoList>) {
        mCache.value = new
    }

}

interface LocalCacheSample {
    fun getTodos(): List<TodoList?>
    fun setTodos(new: List<TodoList>)
    suspend fun getTodosFlow(): Flow<List<TodoList?>>
}

data class TodoList(
    val data: String
)