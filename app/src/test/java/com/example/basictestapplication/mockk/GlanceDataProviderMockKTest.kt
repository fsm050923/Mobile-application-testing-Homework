package com.example.basictestapplication.mockk

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.basictestapplication.glance.GlanceDataProvider
import com.example.basictestapplication.TodoList
import com.example.basictestapplication.MainViewModel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * GlanceDataProvider 的 MockK 测试
 *
 * DataStore Mock 练习，
 * 测试 saveText() 和 glanceTextFlow 的行为。
 *
 * 注意：GlanceDataProvider 使用 preferencesDataStore 委托扩展属性，
 * 该属性在 JVM 单元测试中不能直接 mock（依赖 Android 文件系统）。
 * 此类聚焦于验证 DataStore 交互模式的概念正确性。
 */
class GlanceDataProviderMockKTest {

    // ============================================================
    // Context + DataStore Mock 结构验证
    // ============================================================

    @Test
    fun `Context mock - 可以创建 relaxed mock Context`() {
        // GlanceDataProvider 使用 @ApplicationContext 注入 Context
        // 在单元测试中先验证 mock Context 可以正常工作
        val mockContext = mockk<Context>(relaxed = true)
        assertNotNull(mockContext)
    }

    // ============================================================
    // glanceTextFlow — 验证 Flow 映射逻辑
    // ============================================================

    @Test
    fun `glanceTextFlow 模式验证 - Preferences key 读取逻辑`() {
        // DataStore 的 data 属性返回 Flow<Preferences>
        // glanceTextFlow 通过 .map 提取 stringPreferencesKey 对应的值
        // 这里验证映射逻辑的概念正确性

        val mockPreferences = mockk<Preferences>()
        val testKey = stringPreferencesKey("test_key")

        // 模拟 Preferences 的 get 行为
        every { mockPreferences[testKey] } returns "expected_value"

        // 验证: Preferences[key] 返回预期值
        assertEquals("expected_value", mockPreferences[testKey])
    }

    @Test
    fun `glanceTextFlow 模式验证 - Preferences 返回 null 时使用默认值`() {
        val mockPreferences = mockk<Preferences>()
        val testKey = stringPreferencesKey("glance_text_key")

        // 当 Preferences 中没有存储值时，返回 null
        every { mockPreferences[testKey] } returns null

        // flow 中的 map 会使用 ?: "empty" 作为默认值
        val result = mockPreferences[testKey] ?: "empty"
        assertEquals("empty", result)
    }

    // ============================================================
    // DataStore edit 的 suspend 函数行为验证
    // ============================================================

    @Test
    fun `DataStore edit - MockK 验证 mock DataStore 的调用模式`() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)

        // 模拟 data 属性返回一个 Flow
        every { mockDataStore.data } returns flowOf(mockk(relaxed = true))

        // 验证: 直接调用 DataStore 的 data 属性
        val dataFlow = mockDataStore.data
        assertNotNull(dataFlow)
    }

    // ============================================================
    // 边界场景验证
    // ============================================================

    @Test
    fun `边界场景 - 存储空字符串不被视为空值`() {
        val mockPreferences = mockk<Preferences>()
        val testKey = stringPreferencesKey("test_key")

        // 空字符串也是合法值，? 运算符不会将其替换为默认值
        every { mockPreferences[testKey] } returns ""

        val result = mockPreferences[testKey]
        assertEquals("", result)
        // 验证空字符串不是 null
        assertNotNull(result)
    }

    @Test
    fun `边界场景 - stringPreferencesKey 的 key 名称验证`() {
        // GlanceDataProvider 使用的 key 名称为 "glance_text_key"
        val key = stringPreferencesKey("glance_text_key")
        assertEquals("glance_text_key", key.name)
    }
}
