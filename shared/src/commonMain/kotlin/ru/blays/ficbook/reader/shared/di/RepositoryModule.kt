package ru.blays.ficbook.reader.shared.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.ficbook.reader.shared.data.repo.declaration.*
import ru.blays.ficbook.reader.shared.data.repo.implementation.*
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.preferences.repositiry.SettingsJsonRepository
import ru.blays.ficbook.reader.shared.preferences.repositiry.SettingsRepository

internal val repositoryModule = module {
    // Settings repo's
    singleOf(::SettingsRepository) bind ISettingsRepository::class
    singleOf(::SettingsJsonRepository) bind ISettingsJsonRepository::class

    // Data repo's
    singleOf(::AuthorizationRepo) bind IAuthorizationRepo::class
    singleOf(::AuthorProfileRepo) bind IAuthorProfileRepo::class
    singleOf(::ChaptersRepo) bind IChaptersRepo::class
    singleOf(::CollectionsRepo) bind ICollectionsRepo::class
    singleOf(::CommentsRepo) bind ICommentsRepo::class
    singleOf(::FanficsListRepo) bind IFanficsListRepo::class
    singleOf(::FanficPageRepo) bind IFanficPageRepo::class
    singleOf(::UsersRepo) bind IUsersRepo::class
    singleOf(::NotificationsRepo) bind INotificationsRepo::class
    singleOf(::SearchRepo) bind ISearchRepo::class
    singleOf(::FanficQuickActionsRepo) bind IFanficQuickActionsRepo::class
}