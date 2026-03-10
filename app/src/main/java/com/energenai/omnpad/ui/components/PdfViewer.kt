package com.energenai.omnpad.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energenai.omnpad.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfViewer(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                val fd = context.contentResolver.openFileDescriptor(uri, "r")
                    ?: throw Exception("Cannot open file")
                val renderer = PdfRenderer(fd)
                pageCount = renderer.pageCount
                val rendered = mutableListOf<Bitmap>()

                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    // Render at 2x for readability on high-DPI screens
                    val scale = 2
                    val bitmap = Bitmap.createBitmap(
                        page.width * scale,
                        page.height * scale,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    page.close()
                    rendered.add(bitmap)
                }
                renderer.close()
                fd.close()
                pages = rendered
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    if (error != null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("PDF render failed: $error", color = ErrorRed, fontSize = 14.sp)
        }
        return
    }

    if (pages.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading PDF ($pageCount pages)...", color = TextSecondary, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(pages) { index, bitmap ->
            Column {
                // Page label
                Text(
                    text = "Page ${index + 1} of $pageCount",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                // Rendered page
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Page ${index + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}
