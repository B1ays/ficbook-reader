package ru.blays.ficbook.reader.shared.components.superfilterComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IUsersRepo
import ru.blays.ficbook.reader.shared.utils.listOfInts

internal class DefaultSuperfilterComponent(
    componentContext: ComponentContext,
    private val onOutput: (SuperfilterComponent.Output) -> Unit
): SuperfilterComponent, ComponentContext by componentContext, KoinComponent {
    private val filtersRepo: IFiltersRepo by inject()
    private val usersRepo: IUsersRepo by inject()
    private val searchRepo: ISearchRepo by inject()

    private val pagesNavigation = PagesNavigation<Int>()

    override val pages = childPages(
        source = pagesNavigation,
        initialPages = {
            Pages(listOfInts(4), 0)
        },
        handleBackButton = false,
        serializer = Int.serializer(),
        childFactory = ::childFactory
    )

    private fun childFactory(
        index: Int,
        childContext: ComponentContext
    ): SuperfilterTabComponent {
        return when(index) {
            0 -> SuperfilterAuthorsTabComponent(
                componentContext = childContext,
                filtersRepo = filtersRepo,
                usersRepo = usersRepo
            )
            1 -> SuperfilterFandomsTabComponent(
                componentContext = childContext,
                filtersRepo = filtersRepo,
                searchRepo = searchRepo
            )
            2 -> SuperfilterTagsTabComponent(
                componentContext = childContext,
                filtersRepo = filtersRepo,
                searchRepo = searchRepo
            )
            3 -> SuperfilterDirectionsTabComponent(
                componentContext = childContext,
                filtersRepo = filtersRepo
            )
            else -> error("Unknown tab index: $index")
        }
    }

    override fun changeTab(index: Int) {
        pagesNavigation.select(index)
    }

    override fun onOutput(output: SuperfilterComponent.Output) {
        onOutput.invoke(output)
    }
}