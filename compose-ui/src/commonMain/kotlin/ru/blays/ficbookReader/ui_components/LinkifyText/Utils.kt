package ru.blays.ficbookReader.ui_components.LinkifyText

import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun rememberAnnotatedStringWithLinks(
    text: String,
    linkStyle: SpanStyle,
    normalTextStyle: SpanStyle
): State<AnnotatedString> {
    val annotatedString = remember {
        mutableStateOf(
            AnnotatedString(text)
        )
    }

    LaunchedEffect(text) {
        val matcher = Patterns.AUTOLINK_WEB_URL.matcher(text)

        var normalTextStart = 0
        try {
            annotatedString.value = buildAnnotatedString {
                while (matcher.find()) {
                    val linkStart = matcher.start()
                    val linkEnd = matcher.end()

                    val normalText = text.substring(
                        startIndex = normalTextStart,
                        endIndex = linkStart
                    )
                    withStyle(
                        style = normalTextStyle
                    ) {
                        append(normalText)
                    }
                    normalTextStart = linkEnd

                    val linkText = text.substring(linkStart..<linkEnd)
                    pushStringAnnotation(
                        tag = linkText,
                        annotation = linkText
                    )
                    withStyle(
                        style = linkStyle
                    ) {
                        append(linkText)
                    }
                    pop()
                }
                withStyle(
                    style = normalTextStyle
                ) {
                    append(
                        text.substring(
                            normalTextStart.coerceIn(text.indices)..text.length.coerceIn(text.indices)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return annotatedString
}