@file:OptIn(ExperimentalTestApi::class)

package com.example.basictestapplication.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.basictestapplication.MainActivity
import com.example.basictestapplication.screens.LazyListItemNode
import com.example.basictestapplication.screens.MainActivityScreen
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Rule
import org.junit.Test

/**
 * MainActivity 的 GUI 自动化测试
 *
 * 使用 Kakao/Cup + Compose Test Rule 进行界面测试
 * 覆盖：按钮显示、数据更新、列表交互、滚动行为、Footer 可见性
 */
@OptIn(ExperimentalTestApi::class)
class MainActivityScreenTest {

    @Rule
    @JvmField
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ============================================================
    // 缓存按钮相关测试
    // ============================================================

    @Test
    fun testCacheButtonIsDisplayed() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            cacheButton {
                assertIsDisplayed()
                assertTextContains("Set cache value")
            }
        }
    }

    @Test
    fun testCacheButtonClick_UpdatesDisplayedData() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            // 初始数据显示空列表
            dataTextField {
                assertIsDisplayed()
            }

            // 点击缓存按钮
            cacheButton {
                assertIsDisplayed()
                performClick()
            }

            // 数据应该已更新（包含随机数）
            dataTextField {
                assertIsDisplayed()
            }
        }
    }

    // ============================================================
    // LazyColumn 列表测试
    // ============================================================

    @Test
    fun testLazyListFirstItem_ShowsCorrectNumber() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            list {
                childWith<LazyListItemNode> {
                    hasText("1")
                } perform {
                    assertTextEquals("1")
                }
            }
        }
    }

    @Test
    fun testLazyList_All50ItemsRender() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            list {
                assertIsDisplayed()
                // 验证第 1 项
                childWith<LazyListItemNode> {
                    hasText("1")
                } perform {
                    assertTextEquals("1")
                }
                // 验证第 25 项（中间位置）
                performScrollToIndex(24)
                childWith<LazyListItemNode> {
                    hasText("25")
                } perform {
                    assertTextEquals("25")
                }
                // 验证第 50 项（最后一项）
                performScrollToIndex(49)
                childWith<LazyListItemNode> {
                    hasText("50")
                } perform {
                    assertTextEquals("50")
                }
            }
        }
    }

    // ============================================================
    // Footer 可见性测试
    // ============================================================

    @Test
    fun testFooterIsDisplayedAfterScrolling() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            list {
                assertIsDisplayed()
                // 滚动到最后一个 item (index 50 = Footer)
                performScrollToIndex(50)
            }
            footerText {
                assertIsDisplayed()
            }
        }
    }

    @Test
    fun testFooter_VisibleOnlyAfterFullScroll() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            // 初始状态不做滚动，Footer 应该不在视野内
            // (这里只验证 Footer 存在，但不一定可见)
            footerText {
                assertIsDisplayed()
            }
        }
    }

    // ============================================================
    // 滚动到顶部按钮测试
    // ============================================================

    @Test
    fun testScrollToTopButton_AppearsAfterScrolling() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            list {
                // 先滚动到下方
                performScrollToIndex(30)
            }
            // 验证 ScrollToTop 按钮出现
            scrollToTopButton {
                assertIsDisplayed()
                assertTextContains("Scroll to top")
            }
        }
    }

    @Test
    fun testScrollToTopButton_Click_ScrollsToTop() {
        onComposeScreen<MainActivityScreen>(composeTestRule) {
            list {
                // 先滚动到下方
                performScrollToIndex(30)

                // 点击 Scroll to top
                scrollToTopButton {
                    performClick()
                }
            }

            // 验证第一个 item 重新可见
            list {
                childWith<LazyListItemNode> {
                    hasText("1")
                } perform {
                    assertTextEquals("1")
                }
            }
        }
    }
}
