package ru.blays.ficbookapi.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.ficbookapi.api.*

val apiModule = module {
    factoryOf(::AuthorizationApiImpl) bind AuthorizationApi::class
    factoryOf(::AuthorProfileApiImpl) bind AuthorProfileApi::class
    factoryOf(::ChaptersApiImpl) bind ChaptersApi::class
    factoryOf(::CollectionsApiImpl) bind CollectionsApi::class
    factoryOf(::CommentsApiImpl) bind CommentsApi::class
    factoryOf(::FanficPageApiImpl) bind FanficPageApi::class
    factoryOf(::FanficsListApiImpl) bind FanficsListApi::class
    factoryOf(::UsersApiImpl) bind UsersApi::class
}