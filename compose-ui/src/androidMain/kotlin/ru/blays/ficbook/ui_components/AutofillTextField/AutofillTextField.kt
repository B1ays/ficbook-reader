package ru.blays.ficbook.ui_components.AutofillTextField

import android.annotation.SuppressLint
import android.view.autofill.AutofillManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.Autofill
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun AutofillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    autofillTypes: List<AutofillType>,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?,
    isError: Boolean,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    maxLines: Int,
    minLines: Int,
    interactionSource: MutableInteractionSource,
    shape: Shape,
    colors: TextFieldColors
) {
    Column {
        val autoFillHandler = AutoFillRequestHandler(
            autofillTypes = autofillTypes,
            onFill = {
                onValueChange(it)
            }
        )
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (it.isEmpty()) autoFillHandler.requestVerifyManual()
            },
            modifier = modifier
                .connectNode(handler = autoFillHandler)
                .defaultFocusChangeAutoFill(handler = autoFillHandler)
                .onFocusChanged {
                    if(it.isFocused) autoFillHandler.requestManual()
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors
        )
    }
}


fun Modifier.connectNode(handler: AutoFillHandler): Modifier {
    return with(handler) { fillBounds() }
}

fun Modifier.defaultFocusChangeAutoFill(handler: AutoFillHandler): Modifier {
    return this.then(
        Modifier.onFocusChanged {
            if (it.isFocused) {
                handler.request()
            } else {
                handler.cancel()
            }
        }
    )
}

@SuppressLint("ComposableNaming")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutoFillRequestHandler(
    autofillTypes: List<AutofillType> = listOf(),
    onFill: (String) -> Unit,
): AutoFillHandler {
    val view = LocalView.current
    val context = LocalContext.current
    var isFillRecently = remember { false }
    val autoFillNode = remember {
        AutofillNode(
            autofillTypes = autofillTypes,
            onFill = {
                isFillRecently = true
                onFill(it)
            }
        )
    }
    val autofill = LocalAutofill.current
    LocalAutofillTree.current += autoFillNode
    return remember {
        object : AutoFillHandler {
            val autofillManager = context.getSystemService(AutofillManager::class.java)
            override fun requestManual() {
                autofillManager.requestAutofill(
                    view,
                    autoFillNode.id,
                    autoFillNode.boundingBox?.toAndroidRect() ?: error("BoundingBox is not provided yet")
                )
            }

            override fun requestVerifyManual() {
                if (isFillRecently) {
                    isFillRecently = false
                    requestManual()
                }
            }

            override val autoFill: Autofill?
                get() = autofill

            override val autoFillNode: AutofillNode
                get() = autoFillNode

            override fun request() {
                autofill?.requestAutofillForNode(autofillNode = autoFillNode)
            }

            override fun cancel() {
                autofill?.cancelAutofillForNode(autofillNode = autoFillNode)
            }

            override fun Modifier.fillBounds(): Modifier {
                return this.then(
                    Modifier.onGloballyPositioned {
                        autoFillNode.boundingBox = it.boundsInWindow()
                    })
            }
        }
    }
}

fun Rect.toAndroidRect(): android.graphics.Rect {
    return android.graphics.Rect(
        left.roundToInt(),
        top.roundToInt(),
        right.roundToInt(),
        bottom.roundToInt()
    )
}

@OptIn(ExperimentalComposeUiApi::class)
interface AutoFillHandler {
    val autoFill: Autofill?
    val autoFillNode: AutofillNode
    fun requestVerifyManual()
    fun requestManual()
    fun request()
    fun cancel()
    fun Modifier.fillBounds(): Modifier
}