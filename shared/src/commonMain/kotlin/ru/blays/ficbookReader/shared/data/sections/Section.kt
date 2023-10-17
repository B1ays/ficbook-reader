package ru.blays.ficbookReader.shared.data.sections


import ru.blays.ficbookReader.shared.data.dto.Section
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookapi.data.CategoriesSections
import ru.blays.ficbookapi.data.CollectionsTypes
import ru.blays.ficbookapi.data.PopularSections
import ru.blays.ficbookapi.data.UserSections


object PopularSections {
    val allPopular: Section
        get() = PopularSections._allPopular.toStableModel()
    val gen: Section
        get() = PopularSections._gen.toStableModel()
    val het: Section
        get() = PopularSections._het.toStableModel()
    val slash: Section
        get() = PopularSections._slash.toStableModel()
    val femslash: Section
        get() = PopularSections._femslash.toStableModel()
    val mixed: Section
        get() = PopularSections._mixed.toStableModel()
    val other: Section
        get() = PopularSections._other.toStableModel()
    
    val all = arrayOf(
        allPopular,
        gen,
        het,
        slash,
        femslash,
        mixed,
        other
    )
}

object CategoriesSections {
    val allFandoms: Section
        get() = CategoriesSections._allFandoms.toStableModel()
    val animeAndManga: Section
        get() = CategoriesSections._animeAndManga.toStableModel()
    val books: Section
        get() = CategoriesSections._books.toStableModel()
    val cartoons: Section
        get() = CategoriesSections._cartoons.toStableModel()
    val games: Section
        get() = CategoriesSections._games.toStableModel()
    val movies: Section
        get() = CategoriesSections._movies.toStableModel()
    val other: Section
        get() = CategoriesSections._other.toStableModel()
    val rpf: Section
        get() = CategoriesSections._rpf.toStableModel()
    val originals: Section
        get() = CategoriesSections._originals.toStableModel()
    val comics: Section
        get() = CategoriesSections._comics.toStableModel()
    val musicals: Section
        get() = CategoriesSections._musicals.toStableModel()

    val all: Array<Section> = arrayOf(
        allFandoms,
        animeAndManga,
        books,
        cartoons,
        games,
        movies,
        other,
        rpf,
        originals,
        comics,
        musicals
    )
}

object UserSections {
    val favourites: Section
        get() = UserSections._favourites.toStableModel()
    val liked: Section
        get() = UserSections._liked.toStableModel()
    val readed: Section
        get() = UserSections._readed.toStableModel()
    val follow: Section
        get() = UserSections._follow.toStableModel()
    val visited: Section
        get() = UserSections._visited.toStableModel()

    val all = arrayOf(
        favourites,
        liked,
        readed,
        follow,
        visited
    )
}

object CollectionsTypes {
    val personalCollections: SectionWithQuery
        get() = CollectionsTypes._personalCollections.toStableModel()
    val trackedCollections: SectionWithQuery
        get() = CollectionsTypes._trackedCollections.toStableModel()
    
}