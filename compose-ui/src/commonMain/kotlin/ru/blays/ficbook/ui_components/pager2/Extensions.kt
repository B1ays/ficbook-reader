package ru.blays.ficbook.ui_components.pager2

suspend fun PagerState2<*>.animatedScrollToNextPage() {
    this.animateScrollToPage(currentPage+1)
}

suspend fun PagerState2<*>.animatedScrollToPreviousPage() {
    this.animateScrollToPage(currentPage-1)
}

suspend fun PagerState2<*>.scrollToNextPage() {
    this.scrollToPage(currentPage+1)
}

suspend fun PagerState2<*>.scrollToPreviousPage() {
    this.scrollToPage(currentPage-1)
}