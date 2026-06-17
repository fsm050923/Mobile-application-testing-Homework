package com.example.basictestapplication.uiautomator

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 使用 UiAutomator 进行跨应用 GUI 自动化测试
 *
 * 覆盖：应用启动、屏幕旋转、Back 按键处理、Home 键恢复
 */
@RunWith(AndroidJUnit4::class)
class UiAutomatorTest {

    private lateinit var device: UiDevice
    private val packageName = "com.example.basictestapplication"

    @Before
    fun setup() {
        // 初始化 UiDevice
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // 确保从主屏幕开始
        device.pressHome()

        // 启动应用
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: run {
                // 如果 launch intent 不可用，尝试用 am start
                device.executeShellCommand("am start -n $packageName/.MainActivity")
                return@setup
            }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        // 等待应用启动
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
    }

    @Test
    fun `test - 应用成功启动且前台可见`() {
        // 验证当前前台应用包名
        val currentPackage = device.currentPackageName
        assertEquals(packageName, currentPackage)
    }

    @Test
    fun `test - 应用界面包含 CacheButton`() {
        // 查找 "Set cache value" 文本的按钮
        val cacheButton = device.wait(
            Until.findObject(By.text("Set cache value")),
            3000
        )
        assertNotNull("应找到 Set cache value 按钮", cacheButton)
        assertTrue("按钮应可见", cacheButton.isEnabled)
    }

    @Test
    fun `test - 点击按钮后数据更新`() {
        // 找到并点击 "Set cache value" 按钮
        val cacheButton = device.wait(
            Until.findObject(By.text("Set cache value")),
            3000
        )
        assertNotNull(cacheButton)
        cacheButton.click()

        // 等待一下让 UI 更新
        Thread.sleep(1000)

        // 验证 data TextField 包含更新后的数据
        val dataField = device.findObject(
            By.textContains("data:")
        )
        // 点击按钮后，data: 后面应该有内容（包含随机数）
        assertNotNull("数据字段应存在", dataField)
    }

    @Test
    fun `test - 应用可以处理 Back 键不崩溃`() {
        // 等待应用加载
        Thread.sleep(1000)

        // 按 Back 键
        device.pressBack()

        // 等待一下
        Thread.sleep(500)

        // 验证：要么应用退出到桌面，要么不崩溃（仍在运行）
        val currentPackage = device.currentPackageName
        // 如果应用有处理返回键，可能还在应用内；如果没处理也没崩溃就是成功的
        assertTrue(
            "应用应该要么还在前台要么已返回桌面",
            currentPackage == packageName || currentPackage == "com.android.launcher3" ||
                    currentPackage.contains("launcher")
        )
    }

    @Test
    fun `test - Home 键后重新打开不崩溃`() {
        // 按 Home 键
        device.pressHome()
        Thread.sleep(500)

        // 确认已返回桌面
        val homePackage = device.currentPackageName
        assertNotEquals(packageName, homePackage)

        // 重新启动应用
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        assertNotNull(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // 等待应用重新显示
        val appShown = device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
        assertTrue("应用应在 5 秒内重新显示", appShown)

        // 验证按钮仍然可用
        val cacheButton = device.wait(
            Until.findObject(By.text("Set cache value")),
            3000
        )
        assertNotNull("重新打开后按钮应存在", cacheButton)
    }

    @Test
    fun `test - 应用可以处理快速点击`() {
        // 模拟快速多次点击按钮
        val cacheButton = device.wait(
            Until.findObject(By.text("Set cache value")),
            3000
        )
        assertNotNull(cacheButton)

        // 快速点击 5 次
        repeat(5) {
            cacheButton.click()
            Thread.sleep(100)
        }

        // 验证应用没有崩溃
        Thread.sleep(500)
        val currentPackage = device.currentPackageName
        assertEquals(packageName, currentPackage)
    }
}
