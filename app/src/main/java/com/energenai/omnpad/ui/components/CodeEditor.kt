package com.energenai.omnpad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energenai.omnpad.data.SyntaxHighlighter
import com.energenai.omnpad.ui.theme.*

@Composable
fun CodeEditor(
    content: String,
    language: String?,
    onContentChange: (String) -> Unit,
    readOnly: Boolean = false,
    showLineNumbers: Boolean = true,
    wordWrap: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var textFieldValue by remember(content) {
        mutableStateOf(TextFieldValue(content))
    }

    val lines = content.split('\n')
    val lineCount = lines.size
    val lineNumberWidth = (lineCount.toString().length * 10 + 16).dp
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Row(modifier = modifier.fillMaxSize()) {
        // Line numbers
        if (showLineNumbers) {
            Column(
                modifier = Modifier
                    .width(lineNumberWidth)
                    .fillMaxHeight()
                    .background(SurfaceVariant)
                    .verticalScroll(verticalScroll)
                    .padding(end = 4.dp, top = 8.dp)
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, start = 4.dp),
                        maxLines = 1,
                    )
                }
            }
        }

        // Editor area
        val editorModifier = if (wordWrap) {
            Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
                .padding(8.dp)
        } else {
            Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
                .padding(8.dp)
        }

        if (readOnly) {
            // Read-only: show highlighted text
            val highlighted = remember(content, language) {
                SyntaxHighlighter.highlight(content, language)
            }
            Text(
                text = highlighted,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = editorModifier,
            )
        } else {
            // Editable
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onContentChange(newValue.text)
                },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary,
                ),
                cursorBrush = SolidColor(Accent),
                modifier = editorModifier,
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = TextSecondary.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
