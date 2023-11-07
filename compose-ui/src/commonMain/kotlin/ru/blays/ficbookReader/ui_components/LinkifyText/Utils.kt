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

    LaunchedEffect(Unit) {
        val matcher = Patterns.AUTOLINK_WEB_URL.matcher(text)

        var startIndex = 0
        try {
            annotatedString.value = buildAnnotatedString {
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val value = text.substring(start, end)

                    val substring = text.substring(
                        startIndex = startIndex,
                        endIndex = start
                    )
                    withStyle(
                        style = normalTextStyle
                    ) {
                        append(substring)
                    }
                    startIndex = end

                    pushStringAnnotation(
                        tag = value,
                        annotation = value
                    )
                    withStyle(
                        style = linkStyle
                    ) {
                        append(value)
                    }
                    pop()
                }
                withStyle(
                    style = normalTextStyle
                ) {
                    append(
                        text.substring(
                            startIndex = startIndex.coerceIn(text.indices),
                            endIndex = text.lastIndex.coerceIn(text.indices)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            annotatedString.value = AnnotatedString(
                text = e.message ?: "Error",
            )
        }
    }
    return annotatedString
}