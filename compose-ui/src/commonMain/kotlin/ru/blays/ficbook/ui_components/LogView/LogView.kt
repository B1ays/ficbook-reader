package ru.blays.ficbook.ui_components.LogView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ficbook_reader.`compose-ui`.generated.resources.Res
import ficbook_reader.`compose-ui`.generated.resources.ic_copy
import ficbook_reader.`compose-ui`.generated.resources.ic_share
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.utils.surfaceColorAtAlpha

private const val TAG = "LogViewElement"

@Composable
fun LogView(
    log: String,
    actionCopy: (String) -> Unit,
    actionShare: (String) -> Unit,
) {
    LogView(
        log = AnnotatedString(log),
        actionCopy = actionCopy,
        actionShare = actionShare,
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LogView(
    log: AnnotatedString,
    actionCopy: (String) -> Unit,
    actionShare: (String) -> Unit,
) {

    val scrollState = rememberScrollState()

    /*val register = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/text")
    ) { uri ->
        BLog.i(TAG, "Created uri: $uri")
        uri?.let { actionSaveToFile(it, log.text) }
    }*/

    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {

        Row {
            CustomIconButton(
                onClick = { actionCopy(log.toString()) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.3F),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            CustomIconButton(
                onClick = { actionShare(log.toString()) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.3F),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_share),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            /*Spacer(modifier = Modifier.width(10.dp))

            CustomIconButton(
                onClick = {
                    *//*val logName = "log_${getCurrentFormattedTime(defaultFormatter)}.log"
                    register.launch(logName)*//*
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.3F),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_save_as_24),
                    contentDescription = null
                )
            }*/
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(7.dp),
                text = log
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}