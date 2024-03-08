package ru.blays.ficbook.reader.shared.components.usersComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent

@OptIn(ExperimentalDecomposeApi::class)
class DefaultUsersRootComponent(
    componentContext: ComponentContext,
    private val output: (output: UsersRootComponent.Output) -> Unit
): UsersRootComponent, ComponentContext by componentContext {
    private val authorizationRepo: IAuthorizationRepo by getKoin().inject()

    private val navigation = PagesNavigation<UsersRootComponent.TabConfig>()

    override val tabs = childPages(
        source = navigation,
        initialPages = {
            Pages(
                items = if(authorizationRepo.currentUserModel.value != null) listOf(
                    UsersRootComponent.TabConfig.FavouriteAuthors,
                    UsersRootComponent.TabConfig.PopularAuthors,
                    UsersRootComponent.TabConfig.SearchAuthors
                ) else listOf(
                    UsersRootComponent.TabConfig.PopularAuthors,
                    UsersRootComponent.TabConfig.SearchAuthors
                ),
                selectedIndex = 0
            )
        },
        serializer = UsersRootComponent.TabConfig.serializer(),
        handleBackButton = false
    ) { configuration, componentContext ->
        when(configuration) {
            UsersRootComponent.TabConfig.FavouriteAuthors -> UsersRootComponent.Tabs.FavouriteAuthors(
                DefaultUsersFavouriteComponent(
                    componentContext = componentContext,
                    output = output
                )
            )
            UsersRootComponent.TabConfig.PopularAuthors -> UsersRootComponent.Tabs.PopularAuthors(
                DefaultUsersPopularComponent(
                    componentContext = componentContext,
                    output = output
                )
            )
            UsersRootComponent.TabConfig.SearchAuthors -> UsersRootComponent.Tabs.SearchAuthors(
                DefaultUsersSearchComponent(
                    componentContext = componentContext,
                    output = output
                )
            )
        }
    }

    override fun sendIntent(intent: UsersRootComponent.Intent) {
        when(intent) {
            is UsersRootComponent.Intent.SelectTab -> navigation.select(intent.index)
        }
    }

    override fun onOutput(output: UsersRootComponent.Output) = this.output(output)
}