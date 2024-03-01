/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.blays.ficbook.ui_components.pager2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Creates and remember a [PagerState] to be used with a [Pager]
 *
 * Please refer to the sample to learn how to use this API.
 * @sample androidx.compose.foundation.samples.PagerWithStateSample
 *
 * @param initialPage The pager that should be shown first.
 * @param initialPageOffsetFraction The offset of the initial page as a fraction of the page size.
 * This should vary between -0.5 and 0.5 and indicates how to offset the initial page from the
 * snapped position.
 * @param pageCount The amount of pages this Pager will have.
 */
@Composable
fun <T: Any> rememberPagerState2(
    initialPages: List<T>,
    initialPageIndex: Int = 0,
    initialPageOffsetFraction: Float = 0f,
    serializer: KSerializer<List<T>>
): PagerState2<T> {
    return rememberSaveable(
        saver = PagerStateImpl2.createSaver(serializer)
    ) {
        PagerStateImpl2(
            initialPages = initialPages,
            initialPageIndex = initialPageIndex,
            initialPageOffsetFraction
        )
    }
}

abstract class PagerState2 <T: Any> (
    initialPageIndex: Int,
    initialPageOffsetFraction: Float
): PagerState(
    currentPage = initialPageIndex,
    currentPageOffsetFraction = initialPageOffsetFraction
) {
    abstract val pages: List<T>

    abstract fun updatePages(block: (List<T>) -> List<T>)

    abstract fun addPage(page: T)

    abstract fun addPages(pages: List<T>)

    abstract fun clearPages()
}

@ExperimentalFoundationApi
internal class PagerStateImpl2<T: Any>(
    initialPages: List<T>,
    initialPageIndex: Int,
    initialPageOffsetFraction: Float
) : PagerState2<T>(initialPageIndex, initialPageOffsetFraction) {
    private val _pages: SnapshotStateList<T> = mutableStateListOf<T>().apply {
        addAll(initialPages)
    }

    override val pages get() = _pages

    override fun addPage(page: T) {
        _pages.add(page)
    }

    override fun clearPages() {
        _pages.clear()
    }

    override fun addPages(pages: List<T>) {
        _pages.addAll(pages)

    }

    override fun updatePages(block: (List<T>) -> List<T>) {
        _pages.clear()
        _pages.addAll(block(pages))
    }

    override val pageCount: Int get() = pages.size

    companion object {
        /**
         * To keep current page and current page offset saved
         */
        fun <T: Any> createSaver(
            serializer: KSerializer<List<T>>
        ): Saver<PagerStateImpl2<T>, *> {
            return listSaver(
                save = {

                    val serializedPages = Json.encodeToString(
                        serializer = serializer,
                        value = it.pages
                    )
                    listOf(
                        serializedPages,
                        it.currentPage,
                        it.currentPageOffsetFraction,
                    )

                },
                restore = { list ->
                    val savedPages = Json.decodeFromString(
                        deserializer = serializer,
                        string = list[0] as String
                    )
                    PagerStateImpl2(
                        initialPages = savedPages,
                        initialPageIndex = list[1] as Int,
                        initialPageOffsetFraction = list[2] as Float
                    )
                }
            )
        }
    }
}