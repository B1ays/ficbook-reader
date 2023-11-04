package ru.blays.ficbookReader.ui_components.LinkifyText

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TextWithLinks(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (url: String) -> Unit
) {
    val linkStyle = style.toSpanStyle().copy(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    )
    val normalTextStyle = style.toSpanStyle()
    val annotatedStringWithLinks = rememberAnnotatedStringWithLinks(
        text = text,
        linkStyle = linkStyle,
        normalTextStyle = normalTextStyle
    )
    ClickableText(
        text = annotatedStringWithLinks,
        modifier = modifier,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        onClick = { offset ->
            annotatedStringWithLinks.getStringAnnotations(
                start = offset,
                end = offset
            )
            .firstOrNull()
            ?.let {
                onClick(it.item)
            }
        },
    )
}