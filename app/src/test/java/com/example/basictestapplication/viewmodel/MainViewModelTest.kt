package com.example.basictestapplication.viewmodel

import com.example.basictestapplication.glance.GlanceDataProvider
import com.example.basictestapplication.LocalCacheSampleImpl
import com.example.basictestapplication.MainViewModel
import com.example.basictestapplication.TodoList
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MainViewModel 的单元测试
 *
 * MockK 测试 setDataToCache(), data, getData(), getData2() 等方法
 */
class MainViewModelTest {

    private lateinit var mockCache: LocalCacheSampleImpl
    private lateinit var mockGlanceDataProvider: GlanceDataProvider

    @Before
    fun setup() {
        mockCache = mockk(relaxed = true)
        mockGlanceDataProvider = mockk(relaxed = true)
    }

    // ============================================================
    // data (StateFlow) 测试
    // ============================================================

    @Test
    fun `data - 初始化时不抛异常`() {
        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        assertNotNull(viewModel.data)
    }

    @Test
    fun `data - 返回的 StateFlow 初始值来自 cache`() {
        val flow = MutableStateFlow<List<TodoList?>>(listOf(TodoList("initial")))
        every { mockCache.cache } returns flow.asStateFlow()

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        assertEquals("initial", viewModel.data.value.first()?.data)
    }

    // ============================================================
    // setDataToCache() 测试
    // ============================================================

    @Test
    fun `setDataToCache - 调用后 glanceDataProvider saveText 被调用`() = runTest {
        val flow = MutableStateFlow<List<TodoList?>>(emptyList())
        every { mockCache.setTodos(any()) } just runs
        every { mockCache.cache } returns flow.asStateFlow()
        coEvery { mockGlanceDataProvider.saveText(any()) } just runs

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        viewModel.setDataToCache()

        coVerify(exactly = 1) { mockGlanceDataProvider.saveText(any()) }
    }

    @Test
    fun `setDataToCache - 每次调用 saveText 参数不同（随机性验证）`() = runTest {
        val savedValues = mutableListOf<String>()
        val flow = MutableStateFlow<List<TodoList?>>(emptyList())
        every { mockCache.setTodos(any()) } just runs
        every { mockCache.cache } returns flow.asStateFlow()
        coEvery { mockGlanceDataProvider.saveText(capture(savedValues)) } just runs

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)

        repeat(5) { viewModel.setDataToCache() }

        assertEquals(5, savedValues.size)
        val distinctValues = savedValues.toSet()
        assertTrue("随机数重复过多，可能不是随机生成的", distinctValues.size >= 3)
    }

    // ============================================================
    // getData() 测试
    // ============================================================

    @Test
    fun `getData - 调用返回 cache 的 Flow`() = runTest {
        val flow = MutableStateFlow<List<TodoList?>>(listOf(TodoList("test")))
        coEvery { mockCache.getTodosFlow() } returns flow
        every { mockCache.cache } returns flow.asStateFlow()

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        val result = viewModel.getData()
        val firstEmission: List<TodoList?> = result.first()
        assertEquals("test", firstEmission.first()?.data)
    }

    // ============================================================
    // getData2() 测试
    // ============================================================

    @Test
    fun `getData2 - 返回 cache 中的列表`() {
        val expectedList: List<TodoList?> = listOf(TodoList("a"), TodoList("b"))
        val flow = MutableStateFlow(expectedList)
        every { mockCache.getTodos() } returns expectedList
        every { mockCache.cache } returns flow.asStateFlow()

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        val result = viewModel.getData2()
        assertEquals(2, result.size)
        assertEquals("a", result[0]?.data)
        assertEquals("b", result[1]?.data)
    }

    @Test
    fun `getData2 - 空列表返回`() {
        val flow = MutableStateFlow<List<TodoList?>>(emptyList())
        every { mockCache.getTodos() } returns emptyList()
        every { mockCache.cache } returns flow.asStateFlow()

        val viewModel = MainViewModel(mockCache, mockGlanceDataProvider)
        val result = viewModel.getData2()
        assertTrue(result.isEmpty())
    }
}
