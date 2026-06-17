package com.example.basictestapplication.unit
import com.example.basictestapplication.LocalCacheSampleImpl
import com.example.basictestapplication.TodoList

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * LocalCacheSampleImpl 业务逻辑层的全面单元测试
 *
 * 测试 getTodos(), setTodos(), getTodosFlow(), cache(StateFlow)
 */
class LocalCacheSampleImplTest {

    private lateinit var sut: LocalCacheSampleImpl

    @Before
    fun setup() {
        sut = LocalCacheSampleImpl()
    }

    // ============================================================
    // getTodos() 测试
    // ============================================================

    @Test
    fun `getTodos - 初始状态返回空列表`() {
        val todos = sut.getTodos()
        assertNotNull("不应返回 null", todos)
        assertEquals("初始状态应为空列表", 0, todos.size)
    }

    @Test
    fun `getTodos - 设置一条数据后返回正确数据`() {
        sut.setTodos(listOf(TodoList("Hello tester!")))
        val todos = sut.getTodos()
        assertEquals(1, todos.size)
        assertEquals("Hello tester!", todos.first()?.data)
    }

    @Test
    fun `getTodos - 设置多条数据后返回全部数据`() {
        val input = listOf(
            TodoList("item1"),
            TodoList("item2"),
            TodoList("item3")
        )
        sut.setTodos(input)
        val todos = sut.getTodos()
        assertEquals(3, todos.size)
        assertEquals("item1", todos[0]?.data)
        assertEquals("item2", todos[1]?.data)
        assertEquals("item3", todos[2]?.data)
    }

    @Test
    fun `getTodos - 设置空列表后返回空列表`() {
        sut.setTodos(listOf(TodoList("something")))
        sut.setTodos(emptyList())
        val todos = sut.getTodos()
        assertEquals(0, todos.size)
    }

    // ============================================================
    // setTodos() 测试
    // ============================================================

    @Test
    fun `setTodos - 第二次设置覆盖第一次数据`() {
        sut.setTodos(listOf(TodoList("first")))
        sut.setTodos(listOf(TodoList("second")))
        val todos = sut.getTodos()
        assertEquals(1, todos.size)
        assertEquals("second", todos.first()?.data)
    }

    @Test
    fun `setTodos - 包含空字符串`() {
        sut.setTodos(listOf(TodoList("")))
        val todos = sut.getTodos()
        assertEquals(1, todos.size)
        assertEquals("", todos.first()?.data)
    }

    @Test
    fun `setTodos - 设置大量数据`() {
        val largeList = (1..1000).map { TodoList("item_$it") }
        sut.setTodos(largeList)
        val todos = sut.getTodos()
        assertEquals(1000, todos.size)
        assertEquals("item_1", todos[0]?.data)
        assertEquals("item_1000", todos[999]?.data)
    }

    // ============================================================
    // getTodosFlow() 测试
    // ============================================================

    @Test
    fun `getTodosFlow - 初始状态发射空列表`() = runTest {
        val flow = sut.getTodosFlow()
        val firstEmission = flow.first()
        assertNotNull(firstEmission)
        assertEquals(0, firstEmission.size)
    }

    @Test
    fun `getTodosFlow - setTodos 后发射新数据`() = runTest {
        sut.setTodos(listOf(TodoList("new item")))
        val flow = sut.getTodosFlow()
        val firstEmission = flow.first()
        assertEquals(1, firstEmission.size)
        assertEquals("new item", firstEmission.first()?.data)
    }

    @Test
    fun `getTodosFlow - 多次 setTodos 每次都发射最新数据`() = runTest {
        sut.setTodos(listOf(TodoList("v1")))
        sut.setTodos(listOf(TodoList("v2")))
        sut.setTodos(listOf(TodoList("v3")))
        val flow = sut.getTodosFlow()
        val firstEmission = flow.first()
        assertEquals(1, firstEmission.size)
        assertEquals("v3", firstEmission.first()?.data)
    }

    // ============================================================
    // cache StateFlow 测试
    // ============================================================

    @Test
    fun `cache StateFlow - 初始值为空列表`() {
        assertEquals(0, sut.cache.value.size)
    }

    @Test
    fun `cache StateFlow - setTodos 后 value 更新`() {
        sut.setTodos(listOf(TodoList("test")))
        assertEquals(1, sut.cache.value.size)
        assertEquals("test", sut.cache.value.first()?.data)
    }

    @Test
    fun `cache StateFlow - 连续更新`() {
        sut.setTodos(listOf(TodoList("a")))
        sut.setTodos(listOf(TodoList("b")))
        sut.setTodos(listOf(TodoList("c")))
        assertEquals("c", sut.cache.value.first()?.data)
    }

    // ============================================================
    // 边界条件测试
    // ============================================================

    @Test
    fun `边界条件 - 未调用 setTodos 前多次调用 getTodos 返回一致`() {
        val result1 = sut.getTodos()
        val result2 = sut.getTodos()
        val result3 = sut.getTodos()
        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(0, result3.size)
    }

    @Test
    fun `边界条件 - getTodosFlow 和 getTodos 数据一致性`() = runTest {
        sut.setTodos(listOf(TodoList("sync_test")))
        val flowResult = sut.getTodosFlow().first()
        val directResult = sut.getTodos()
        assertEquals(directResult, flowResult)
    }
}
