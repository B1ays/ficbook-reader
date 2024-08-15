package ru.blays.ficbook.ui_components.Text

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import coil3.annotation.InternalCoilApi
import coil3.compose.SubcomposeAsyncImage
import coil3.util.MimeTypeMap
import ru.blays.ficbook.values.IntIntPair

@Composable
fun TextWithInlineImages(
    modifier: Modifier = Modifier,
    fullText: String,
    textStyle: TextStyle = TextStyle.Default,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    linkTextColor: Color = MaterialTheme.colorScheme.primary,
    linkTextFontWeight: FontWeight = FontWeight.SemiBold,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
    onLinkClick: (link: String) -> Unit
) {
    val linkStyle = SpanStyle(
        color = linkTextColor,
        fontSize = fontSize,
        fontWeight = linkTextFontWeight,
        textDecoration = linkTextDecoration
    )
    val normalTextStyle = SpanStyle(
        fontSize = fontSize
    )

    val splittedText = remember(fullText) {
        splitText(
            text = fullText,
            linkStyle = linkStyle,
            normalTextStyle = normalTextStyle,
            onLinkClick = onLinkClick
        )
    }

    Column(
        modifier = modifier
    ) {
        splittedText.forEach { part ->
            when(part) {
                is AnnotatedString -> {
                    Text(
                        text = part,
                        style = textStyle,
                        overflow = overflow,
                        maxLines = maxLines
                    )
                }
                is String -> {
                    SubcomposeAsyncImage(
                        model = part,
                        contentDescription = null,
                        error = {
                            Text(
                                text = buildAnnotatedString {
                                    append(part)
                                    addLink(
                                        clickable = LinkAnnotation.Clickable(
                                            tag = part,
                                            styles = TextLinkStyles(linkStyle)
                                        ) {
                                            onLinkClick(part)
                                        },
                                        start = 0,
                                        end = part.length
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(InternalCoilApi::class)
private fun splitText(
    text: String,
    linkStyle: SpanStyle,
    normalTextStyle: SpanStyle,
    onLinkClick: (link: String) -> Unit
): List<Any> {
    val result: MutableList<Any> = mutableListOf()

    val matcher = Patterns.AUTOLINK_WEB_URL.matcher(text)
    val mimeTypeMap = MimeTypeMap

    var previousGroupEnd = 0
    val urlsMap: MutableMap<IntIntPair, String> = mutableMapOf()

    val textLinkStyles = TextLinkStyles(linkStyle)

    while(matcher.find()) {
        val url = matcher.group()
        val start = matcher.start()
        val end = matcher.end()

        val mediaType = mimeTypeMap.getMimeTypeFromUrl(url)
        if(mediaType != null && mediaType.startsWith("image")) {
            if(previousGroupEnd > 0) {
                val part = text.substring(previousGroupEnd, start)
                result += buildAnnotatedString {
                    withStyle(normalTextStyle) {
                        append(part)
                    }
                    urlsMap.forEach { (pair, url) ->
                        addLink(
                            clickable = LinkAnnotation.Clickable(
                                tag = url,
                                styles = textLinkStyles
                            ) { onLinkClick(url) },
                            start = pair.first,
                            end = pair.second
                        )
                    }
                }
            }
            result += url
            previousGroupEnd = end + 1
            urlsMap.clear()
        } else {
            val lStart = start - previousGroupEnd
            val lEnd = end - previousGroupEnd
            urlsMap[IntIntPair(lStart, lEnd)] = url
        }
    }

    if(previousGroupEnd < text.lastIndex) {
        result += buildAnnotatedString {
            withStyle(normalTextStyle) {
                val part = text.substring(previousGroupEnd, text.length)
                append(part)
                urlsMap.forEach { (pair, url) ->
                    addLink(
                        clickable = LinkAnnotation.Clickable(
                            tag = url,
                            styles = textLinkStyles
                        ) { onLinkClick(url) },
                        start = pair.first,
                        end = pair.second
                    )
                }
            }
        }
    }

    return result
}
