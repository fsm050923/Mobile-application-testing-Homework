package com.example.basictestapplication.unit
import com.example.basictestapplication.TodoList

import org.junit.Assert.*
import org.junit.Test

/**
 * TodoList 数据类的单元测试
 *
 * 测试构造函数, equals(), copy(), toString()
 * 注意: TodoList.data 是 String 类型(非空)
 */
class TodoListTest {

    // ============================================================
    // 构造函数测试
    // ============================================================

    @Test
    fun `构造函数 - 传入普通字符串`() {
        val todo = TodoList("test_data")
        assertEquals("test_data", todo.data)
    }

    @Test
    fun `构造函数 - 传入空字符串`() {
        val todo = TodoList("")
        assertEquals("", todo.data)
    }

    @Test
    fun `构造函数 - 传入特殊字符`() {
        val specialString = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val todo = TodoList(specialString)
        assertEquals(specialString, todo.data)
    }

    @Test
    fun `构造函数 - 传入超长字符串`() {
        val longString = "a".repeat(10000)
        val todo = TodoList(longString)
        assertEquals(longString, todo.data)
    }

    @Test
    fun `构造函数 - 传入中文字符`() {
        val todo = TodoList("测试数据中文")
        assertEquals("测试数据中文", todo.data)
    }

    // ============================================================
    // equals() 测试
    // ============================================================

    @Test
    fun `equals - 相同 data 的两个对象应相等`() {
        val todo1 = TodoList("hello")
        val todo2 = TodoList("hello")
        assertEquals(todo1, todo2)
        assertTrue(todo1 == todo2)
    }

    @Test
    fun `equals - 不同 data 的两个对象应不相等`() {
        val todo1 = TodoList("hello")
        val todo2 = TodoList("world")
        assertNotEquals(todo1, todo2)
        assertFalse(todo1 == todo2)
    }

    @Test
    fun `equals - 自己与自己相等（自反性）`() {
        val todo = TodoList("data")
        assertEquals(todo, todo)
    }

    @Test
    fun `equals - 与不同类型的对象不相等`() {
        val todo = TodoList("data")
        assertNotEquals(todo, "data") // String vs TodoList
        assertNotEquals(todo, Any())
    }

    // ============================================================
    // copy() 测试
    // ============================================================

    @Test
    fun `copy - 不传参数保持原 data`() {
        val original = TodoList("original")
        val copied = original.copy()
        assertEquals(original, copied)
        assertEquals(original.data, copied.data)
    }

    @Test
    fun `copy - 传入新 data 替换原值`() {
        val original = TodoList("original")
        val copied = original.copy(data = "new_data")
        assertEquals("new_data", copied.data)
        assertNotEquals(original, copied)
    }

    @Test
    fun `copy - 原对象不受修改影响（不可变性）`() {
        val original = TodoList("original")
        original.copy(data = "modified")
        // 原对象应该保持不变
        assertEquals("original", original.data)
    }

    // ============================================================
    // toString() 测试
    // ============================================================

    @Test
    fun `toString - 包含 data 值`() {
        val todo = TodoList("my_data")
        val str = todo.toString()
        assertTrue("toString 应包含 data 值", str.contains("my_data"))
    }

    @Test
    fun `toString - 空字符串 data`() {
        val todo = TodoList("")
        val str = todo.toString()
        assertNotNull(str)
    }

    // ============================================================
    // hashCode() 测试（由 data class 自动生成）
    // ============================================================

    @Test
    fun `hashCode - 相同 data 的对象 hashCode 相同`() {
        val todo1 = TodoList("hello")
        val todo2 = TodoList("hello")
        assertEquals(todo1.hashCode(), todo2.hashCode())
    }

    @Test
    fun `hashCode - 不同 data 的对象 hashCode 通常不同`() {
        val todo1 = TodoList("hello")
        val todo2 = TodoList("world")
        assertNotEquals(todo1.hashCode(), todo2.hashCode())
    }

    // ============================================================
    // component1() 解构声明测试
    // ============================================================

    @Test
    fun `解构声明 - component1 返回 data`() {
        val todo = TodoList("test")
        val (data) = todo
        assertEquals("test", data)
    }
}
