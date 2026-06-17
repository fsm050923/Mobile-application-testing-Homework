package com.example.basictestapplication.screens

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasTestTag
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import io.github.kakaocup.compose.node.element.lazylist.KLazyListItemNode
import io.github.kakaocup.compose.node.element.lazylist.KLazyListNode

class MainActivityScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MainActivityScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("MainScreen") }
    ) {

    val cacheButton: KNode = child {
        hasTestTag("CacheButton")
    }

    val dataTextField: KNode = child {
        hasTestTag("DataTextField")
    }

    val footerText: KNode = child {
        hasTestTag("FooterTextField")
    }

    val scrollToTopButton: KNode = child {
        hasTestTag("ScrollToTopButton")
    }

    val list = KLazyListNode(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("SampleList") },
        itemTypeBuilder = {
            itemType(::LazyListItemNode)
            itemType(::LazyListHeaderNode)
        },
        positionMatcher =  { position -> hasTestTag("position=$position") }
    )

}

class LazyListItemNode(
    semanticsNode: SemanticsNode,
    semanticsProvider: SemanticsNodeInteractionsProvider,
) : KLazyListItemNode<LazyListItemNode>(semanticsNode, semanticsProvider)

class LazyListHeaderNode(
    semanticsNode: SemanticsNode,
    semanticsProvider: SemanticsNodeInteractionsProvider,
) : KLazyListItemNode<LazyListHeaderNode>(semanticsNode, semanticsProvider)