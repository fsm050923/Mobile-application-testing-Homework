package com.example.basictestapplication.unit

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import com.example.basictestapplication.worker.WidgetWorkerAdapter
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

/**
 * WidgetWorkerAdapter 的单元测试
 *
 * 测试 startWidgetAppWorker()
 * 验证 WorkManager 调用
 */
class WidgetWorkerAdapterTest {

    @Test
    fun `startWidgetAppWorker - 调用 WorkManager enqueueUniquePeriodicWork`() {
        // Given: 创建一个 mock 的 WorkManager
        val mockWorkManager = mockk<WorkManager>()
        val mockOperation = mockk<Operation>(relaxed = true)

        // enqueueUniquePeriodicWork 是 Java 方法，返回 Operation 类型
        every {
            mockWorkManager.enqueueUniquePeriodicWork(
                any<String>(),
                any<ExistingPeriodicWorkPolicy>(),
                any()
            )
        } returns mockOperation

        val adapter = WidgetWorkerAdapter(mockWorkManager)

        // When: 调用 startWidgetAppWorker
        adapter.startWidgetAppWorker()

        // Then: 验证 enqueueUniquePeriodicWork 被调用
        verify(exactly = 1) {
            mockWorkManager.enqueueUniquePeriodicWork(
                any<String>(),
                any<ExistingPeriodicWorkPolicy>(),
                any()
            )
        }
    }

    @Test
    fun `startWidgetAppWorker - 多次调用每次都重新入队`() {
        // Given
        val mockWorkManager = mockk<WorkManager>()
        val mockOperation = mockk<Operation>(relaxed = true)

        every {
            mockWorkManager.enqueueUniquePeriodicWork(
                any<String>(),
                any<ExistingPeriodicWorkPolicy>(),
                any()
            )
        } returns mockOperation

        val adapter = WidgetWorkerAdapter(mockWorkManager)

        // When: 多次调用
        adapter.startWidgetAppWorker()
        adapter.startWidgetAppWorker()
        adapter.startWidgetAppWorker()

        // Then: 每次调用都触发入队
        verify(exactly = 3) {
            mockWorkManager.enqueueUniquePeriodicWork(
                any<String>(),
                any<ExistingPeriodicWorkPolicy>(),
                any()
            )
        }
    }

    @Test
    fun `startWidgetAppWorker - 验证传入的策略是 CANCEL_AND_REENQUEUE`() {
        // Given
        val mockWorkManager = mockk<WorkManager>()
        val mockOperation = mockk<Operation>(relaxed = true)

        val policySlot = slot<ExistingPeriodicWorkPolicy>()
        every {
            mockWorkManager.enqueueUniquePeriodicWork(
                any<String>(),
                capture(policySlot),
                any()
            )
        } returns mockOperation

        val adapter = WidgetWorkerAdapter(mockWorkManager)

        // When
        adapter.startWidgetAppWorker()

        // Then: 验证策略是 CANCEL_AND_REENQUEUE
        assertEquals(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, policySlot.captured)
    }
}
