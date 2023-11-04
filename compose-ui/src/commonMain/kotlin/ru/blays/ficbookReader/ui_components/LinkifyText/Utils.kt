package ru.blays.ficbookReader.ui_components.LinkifyText

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun rememberAnnotatedStringWithLinks(
    text: String,
    linkStyle: SpanStyle,
    normalTextStyle: SpanStyle
): AnnotatedString {
    return remember {
        val matcher = Patterns.AUTOLINK_WEB_URL.matcher(text)
        val urls = mutableListOf<Url>()
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val value = text.substring(start, end)
            urls += Url(start, end, value)
        }
        var startIndex = 0
        val annotatedString = buildAnnotatedString {
            for (url in urls) {
                val substring = text.substring(
                    startIndex = startIndex,
                    endIndex = url.start
                )
                withStyle(
                    style = normalTextStyle
                ) {
                    append(substring)
                }
                startIndex = url.end
                pushStringAnnotation(
                    tag = url.value,
                    annotation = url.value
                )
                withStyle(
                    style = linkStyle
                ) {
                    append(url.value)
                }
                pop()
            }
            withStyle(
                style = normalTextStyle
            ) {
                append(
                    text.substring(
                    startIndex = startIndex.coerceAtMost(text.length),
                    endIndex = text.length
                    )
                )
            }
        }
        return@remember annotatedString
    }
}


private data class Url(
    val start: Int,
    val end: Int,
    val value: String
)