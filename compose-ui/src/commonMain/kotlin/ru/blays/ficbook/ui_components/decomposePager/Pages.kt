package ru.blays.ficbook.ui_components.decomposePager

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value

/**
 * Displays a list of pages represented by [ChildPages].
 */
@ExperimentalFoundationApi
@ExperimentalDecomposeApi
@Composable
fun <T : Any> Pages(
    pages: Value<ChildPages<*, T>>,
    onPageSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    scrollAnimation: PagesScrollAnimation = PagesScrollAnimation.Disabled,
    pager: Pager = defaultHorizontalPager(),
    pageContent: @Composable PagerScope.(index: Int, page: T) -> Unit,
) {
    val state = pages.subscribeAsState()

    Pages(
        pages = state,
        onPageSelected = onPageSelected,
        modifier = modifier,
        userScrollEnabled = userScrollEnabled,
        scrollAnimation = scrollAnimation,
        pager = pager,
        pageContent = pageContent,
    )
}

/**
 * Displays a list of pages represented by [ChildPages].
 */
@OptIn(InternalDecomposeApi::class)
@ExperimentalFoundationApi
@ExperimentalDecomposeApi
@Composable
fun <T : Any> Pages(
    pages: State<ChildPages<*, T>>,
    onPageSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    scrollAnimation: PagesScrollAnimation = PagesScrollAnimation.Disabled,
    pager: Pager = defaultHorizontalPager(),
    pageContent: @Composable PagerScope.(index: Int, page: T) -> Unit,
) {
    val childPages by pages
    val selectedIndex = childPages.selectedIndex
    val state = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { childPages.items.size },
    )

    LaunchedEffect(selectedIndex) {
        if (state.currentPage != selectedIndex) {
            when (scrollAnimation) {
                is PagesScrollAnimation.Disabled -> state.scrollToPage(selectedIndex)
                is PagesScrollAnimation.Default -> state.animateScrollToPage(page = selectedIndex)
                is PagesScrollAnimation.Custom -> state.animateScrollToPage(page = selectedIndex, animationSpec = scrollAnimation.spec)
            }
        }
    }

    DisposableEffect(state.currentPage) {
        if (state.currentPage == state.targetPage) {
            onPageSelected(state.currentPage)
        }

        onDispose {}
    }

    pager(
        modifier,
        userScrollEnabled,
        state,
        { childPages.items[it].configuration.hashString() },
    ) { pageIndex ->
        val item = childPages.items[pageIndex]

        val pageRef = remember(item.configuration) { Ref(item.instance) }
        if (item.instance != null) {
            pageRef.value = item.instance
        }

        val page = pageRef.value
        if (page != null) {
            pageContent(pageIndex, page)
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalDecomposeApi
fun defaultHorizontalPager(): Pager =
    { modifier, userScrollEnabled, state, key, pageContent ->
        HorizontalPager(
            modifier = modifier,
            userScrollEnabled = userScrollEnabled,
            state = state,
            key = key,
            pageContent = pageContent,
        )
    }

@ExperimentalFoundationApi
@ExperimentalDecomposeApi
fun defaultVerticalPager(): Pager =
    { modifier, userScrollEnabled, state, key, pageContent ->
        VerticalPager(
            modifier = modifier,
            userScrollEnabled = userScrollEnabled,
            state = state,
            key = key,
            pageContent = pageContent,
        )
    }

@OptIn(ExperimentalFoundationApi::class)
internal typealias Pager =
        @Composable (
            Modifier,
            Boolean,
            PagerState,
            key: (index: Int) -> Any,
            pageContent: @Composable PagerScope.(index: Int) -> Unit,
        ) -> Unit


@ExperimentalFoundationApi
@ExperimentalDecomposeApi
sealed interface PagesScrollAnimation {

    data object Disabled : PagesScrollAnimation
    data object Default : PagesScrollAnimation
    class Custom(val spec: AnimationSpec<Float>) : PagesScrollAnimation
}

@InternalDecomposeApi
class Ref<T>(var value: T)