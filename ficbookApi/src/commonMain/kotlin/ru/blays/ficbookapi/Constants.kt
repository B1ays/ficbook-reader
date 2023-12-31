package ru.blays.ficbookapi

const val FICBOOK_HOST = "ficbook.net"

const val HTTPS_SCHEME = "https"

const val SETTING_HREF = "home/settings"
const val RANDOM_FANFIC = "randomfic"
const val READFIC_HREF = "readfic"
const val AUTHORS_HREF = "authors"
const val LOGIN_CHECK = "login_check"
const val PART_COMMENTS_HREF = "comments/get_fanfic_part_comments"
const val FAVOURITE_AUTHORS_HREF = "home/favourites/authors"
const val AUTHOR_SEARCH_HREF = "authors/search"
const val NOTIFICATIONS_HREF = "notifications"
const val SEARCH_HREF = "find-fanfics-846555"
const val SEARCH_FANDOMS_HREF = "fandoms/search"
const val SEARCH_TAGS_HREF = "tags/search"
const val SEARCH_CHARACTERS_HREF = "ajax/fandoms/characters"
const val COMMENT_ADD_HREF = "comment/add"

const val QUERY_PAGE = "p"
const val QUERY_TAB = "tab"

const val SUFFIX_PART_CONTENT = "#part_content"

const val ATTR_HREF = "href"
const val ATTR_SRC = "src"
const val ATTR_VALUE = "value"
const val ATTR_NAME = "name"


/* ---- Regex constants ---- */
val notNumberRegex = Regex("[^0-9]+")