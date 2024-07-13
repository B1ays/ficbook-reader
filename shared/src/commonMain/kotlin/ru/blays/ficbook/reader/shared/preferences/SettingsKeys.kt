package ru.blays.ficbook.reader.shared.preferences

object SettingsKeys {

    // Theme
    const val THEME_KEY = "theme_code"
    const val AMOLED_THEME_KEY = "amoled_theme"
    const val ACCENT_INDEX_KEY = "accent_index"
    const val DYNAMIC_COLORS_KEY = "dynamic_colors"

    // Glass effect settings
    const val GLASS_EFFECT_ENABLED_KEY = "glass_effect"
    const val GLASS_EFFECT_ALPHA_KEY = "glass_effect_alpha"
    const val GLASS_EFFECT_RADIUS_KEY = "glass_effect_blur_radius"
    const val GLASS_EFFECT_NOISE_FACTOR_KEY = "glass_effect_noise_factor"

    // Feed
    const val FEED_SECTION_KEY = "feed_section"

    // Collection
    const val COLLECTION_SORT_TYPE_KEY = "collection_sort"

    // Reader
    const val READER_PREFS_KEY = "ReaderPrefs"
    const val TYPOGRAF_KEY = "typograf_enabled"

    // Fanfic Page
    const val REVERSE_CHAPTERS_ORDER = "reverse_chapters_order"

    // Common
    const val SUPERFILTER_KEY: String = "superfilter"
    const val AUTO_VOTE_FOR_CONTINUE: String = "auto_vote_for_continue"
    const val CHROME_CUSTOM_TABS_KEY: String = "chrome_custom_tabs"
    const val ANONYMOUS_MODE_KEY: String = "anonymous_mode"
    const val ACTIVE_USER_ID_KEY: String = "active_user_id"

    // Proxy
    const val PROXY_ENABLED_KEY = "proxy_enabled"
    const val PROXY_USE_CUSTOM_KEY = "proxy_use_custom"
    const val PROXY_CONFIG_KEY = "proxy_config"

    // Internal
    const val FIRST_START_KEY = "first_start"

}