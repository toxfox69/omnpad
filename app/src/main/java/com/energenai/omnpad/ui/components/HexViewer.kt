package com.energenai.omnpad.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energenai.omnpad.ui.theme.*

@Composable
fun HexViewer(bytes: ByteArray, modifier: Modifier = Modifier) {
    val rows = (bytes.size + 15) / 16

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(rows) { row ->
            val offset = row * 16
            val end = minOf(offset + 16, bytes.size)
            val rowBytes = bytes.sliceArray(offset until end)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 1.dp)
            ) {
                // Offset
                Text(
                    text = "%08X".format(offset),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.width(72.dp)
                )

                Spacer(Modifier.width(8.dp))

                // Hex bytes
                Text(
                    text = rowBytes.joinToString(" ") { "%02X".format(it) }
                        .padEnd(47), // 16*3 - 1
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Accent,
                    modifier = Modifier.width(300.dp)
                )

                Spacer(Modifier.width(8.dp))

                // ASCII representation
                Text(
                    text = rowBytes.map { b ->
                        val c = b.toInt() and 0xFF
                        if (c in 32..126) c.toChar() else '.'
                    }.joinToString(""),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Magenta,
                )
            }
        }
    }
}
