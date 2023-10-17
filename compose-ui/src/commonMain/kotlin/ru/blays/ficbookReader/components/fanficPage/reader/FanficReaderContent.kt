@file:JvmName("FanficReaderContentCommon")

package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.runtime.Composable
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.platformUtils.landscapeModeWidth
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent

@Composable
fun FanficReaderContent(component: MainReaderComponent) {

    val windowSize = WindowSize()

    if(windowSize.width > landscapeModeWidth) {
        LandscapeContent(component)
    } else {
        PortraitContent(component)
    }
}

@Composable
expect fun LandscapeContent(component: MainReaderComponent)

@Composable
expect fun PortraitContent(component: MainReaderComponent)


/*@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Reader(
    text: String,
    style: TextStyle,
    fullscreenMode: Boolean,
    isLoading: Boolean,
    component: MainReaderComponent,
    onChapterChange: (newChapterIndex: Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    FullscreenContainer(
        enabled = fullscreenMode
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ReaderTopBarContent(
                modifier = Modifier.weight(0.04F),
                component = component
            )
            BoxWithConstraints(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .weight(0.92F)
            ) {
                val pages = rememberTextPages(
                    text = text,
                    style = style,
                    constraints = this@BoxWithConstraints.constraints
                )
                val pagerState = rememberPagerState {
                    pages.size
                }

                if(!isLoading) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                val firstTapZone = 0F..size.width*0.33333F
                                val midTapZone = size.width*0.33333F..size.width*0.66666F
                                val secondTapZone = size.width*0.66666F..size.width.toFloat()
                                detectTapGestures {
                                    when(it.x) {
                                        in firstTapZone -> {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage-1
                                                )
                                            }
                                        }
                                        in midTapZone -> {
                                            onSettingsClick()
                                        }
                                        in secondTapZone -> {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage+1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    ) { page ->
                        Text(
                            text = pages[page],
                            style = style,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxWidth(0.4F)
                        )
                    }
                }

                *//*ReaderControl(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    isLoading = isLoading,
                    haveNextChapter = haveNextChapter,
                    havePreviousChapter = havePreviousChapter,
                    onFirstPage = onFirstPage,
                    onLastPage = onLastPage,
                    readerState = readerState,
                    pagerState = pagerState,
                    onChapterChange = {
                        scope.launch {
                            pagerState.scrollToPage(0)
                        }
                        onChapterChange(it)
                    }
                )*//*
            }
            ReaderTopBarContent(
                modifier = Modifier.weight(0.04F),
                component = component
            )
        }
    }
}*/