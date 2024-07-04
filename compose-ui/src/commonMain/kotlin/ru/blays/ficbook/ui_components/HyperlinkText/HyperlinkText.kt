package ru.blays.ficbook.ui_components.HyperlinkText

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

@Composable
fun HyperlinkText(
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

    val annotatedString = remember(fullText) {
        buildAnnotatedString {
            append(fullText)

            val matcher = Patterns.AUTOLINK_WEB_URL.matcher(fullText)

            while(matcher.find()) {
                val link = matcher.group()
                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = link,
                        styles = TextLinkStyles(linkStyle)
                    ) {
                        onLinkClick(link)
                    },
                    start = matcher.start(),
                    end = matcher.end()
                )
            }

            addStyle(
                style = normalTextStyle,
                start = 0,
                end = fullText.length
            )
        }
    }

    Text(
        modifier = modifier,
        text = annotatedString,
        style = textStyle,
        overflow = overflow,
        maxLines = maxLines,
    )
}