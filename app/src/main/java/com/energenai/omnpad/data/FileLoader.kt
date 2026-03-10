package com.energenai.omnpad.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

data class LoadedFile(
    val name: String,
    val uri: Uri,
    val type: FileType,
    val textContent: String? = null,
    val rawBytes: ByteArray? = null,
    val size: Long = 0,
    val encoding: String = "UTF-8",
) {
    val isEditable: Boolean
        get() = type.category in setOf(
            FileCategory.TEXT, FileCategory.CODE,
            FileCategory.MARKDOWN, FileCategory.DATA
        )
}

object FileLoader {

    suspend fun load(context: Context, uri: Uri): LoadedFile = withContext(Dispatchers.IO) {
        val name = getFileName(context, uri) ?: "unnamed"
        val type = FileTypes.detect(name)
        val size = getFileSize(context, uri)

        when (type.category) {
            FileCategory.TEXT, FileCategory.CODE, FileCategory.MARKDOWN, FileCategory.DATA -> {
                val text = readText(context, uri)
                LoadedFile(name, uri, type, textContent = text, size = size)
            }
            FileCategory.PDF -> {
                val text = extractPdfText(context, uri)
                val bytes = readBytes(context, uri)
                LoadedFile(name, uri, type, textContent = text, rawBytes = bytes, size = size)
            }
            FileCategory.OFFICE -> {
                val text = extractOfficeText(context, uri, type)
                LoadedFile(name, uri, type, textContent = text, size = size)
            }
            FileCategory.IMAGE -> {
                LoadedFile(name, uri, type, size = size)
            }
            FileCategory.AUDIO, FileCategory.VIDEO -> {
                LoadedFile(name, uri, type, size = size)
            }
            FileCategory.ARCHIVE -> {
                val listing = listArchiveContents(context, uri)
                LoadedFile(name, uri, type, textContent = listing, size = size)
            }
            FileCategory.BINARY -> {
                val bytes = readBytes(context, uri, limit = 64 * 1024) // 64KB for hex view
                val text = tryReadAsText(context, uri)
                if (text != null) {
                    // It was actually text — upgrade
                    val upgradedType = type.copy(category = FileCategory.TEXT)
                    LoadedFile(name, uri, upgradedType, textContent = text, size = size)
                } else {
                    LoadedFile(name, uri, type, rawBytes = bytes, size = size)
                }
            }
        }
    }

    private fun readText(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
        } ?: ""
    }

    private fun tryReadAsText(context: Context, uri: Uri): String? {
        return try {
            val bytes = readBytes(context, uri, limit = 8192)
            if (bytes != null && isLikelyText(bytes)) {
                readText(context, uri)
            } else null
        } catch (_: Exception) { null }
    }

    private fun isLikelyText(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return true
        var nonPrintable = 0
        val check = minOf(bytes.size, 4096)
        for (i in 0 until check) {
            val b = bytes[i].toInt() and 0xFF
            if (b == 0) return false  // Null byte = definitely binary
            if (b < 32 && b != 9 && b != 10 && b != 13) nonPrintable++
        }
        return nonPrintable.toFloat() / check < 0.05f
    }

    private fun readBytes(context: Context, uri: Uri, limit: Int = Int.MAX_VALUE): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                var total = 0
                var read: Int
                while (stream.read(chunk).also { read = it } != -1 && total < limit) {
                    val toWrite = minOf(read, limit - total)
                    buffer.write(chunk, 0, toWrite)
                    total += toWrite
                }
                buffer.toByteArray()
            }
        } catch (_: Exception) { null }
    }

    private fun extractPdfText(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(stream)
                val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                val text = stripper.getText(doc)
                doc.close()
                text
            } ?: "[Could not read PDF]"
        } catch (e: Exception) {
            "[PDF extraction failed: ${e.message}]"
        }
    }

    private fun extractOfficeText(context: Context, uri: Uri, type: FileType): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                when (type.extension) {
                    "docx" -> {
                        val doc = XWPFDocument(stream)
                        val sb = StringBuilder()
                        doc.paragraphs.forEach { p -> sb.appendLine(p.text) }
                        doc.tables.forEach { table ->
                            sb.appendLine("\n--- TABLE ---")
                            table.rows.forEach { row ->
                                sb.appendLine(row.tableCells.joinToString(" | ") { it.text })
                            }
                        }
                        doc.close()
                        sb.toString()
                    }
                    "doc" -> {
                        "[Legacy .doc format — save as .docx to edit]"
                    }
                    "xlsx", "xls" -> {
                        val wb = WorkbookFactory.create(stream)
                        val sb = StringBuilder()
                        for (i in 0 until wb.numberOfSheets) {
                            val sheet = wb.getSheetAt(i)
                            sb.appendLine("=== Sheet: ${sheet.sheetName} ===\n")
                            for (row in sheet) {
                                val cells = (0 until row.lastCellNum).map { col ->
                                    row.getCell(col)?.toString() ?: ""
                                }
                                sb.appendLine(cells.joinToString("\t"))
                            }
                            sb.appendLine()
                        }
                        wb.close()
                        sb.toString()
                    }
                    "pptx" -> {
                        val pptx = XMLSlideShow(stream)
                        val sb = StringBuilder()
                        pptx.slides.forEachIndexed { idx, slide ->
                            sb.appendLine("=== Slide ${idx + 1} ===")
                            slide.shapes.forEach { shape ->
                                if (shape is org.apache.poi.xslf.usermodel.XSLFTextShape) {
                                    sb.appendLine(shape.text)
                                }
                            }
                            sb.appendLine()
                        }
                        pptx.close()
                        sb.toString()
                    }
                    else -> "[Unsupported Office format: ${type.extension}]"
                }
            } ?: "[Could not open file]"
        } catch (e: Exception) {
            "[Office extraction failed: ${e.message}]"
        }
    }

    private fun listArchiveContents(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val sb = StringBuilder("Archive contents:\n\n")
                val buffered = java.io.BufferedInputStream(stream)
                val factory = org.apache.commons.compress.archivers.ArchiveStreamFactory()
                val archive: org.apache.commons.compress.archivers.ArchiveInputStream<out org.apache.commons.compress.archivers.ArchiveEntry> =
                    factory.createArchiveInputStream(buffered)
                var count = 0
                var entry: org.apache.commons.compress.archivers.ArchiveEntry? = archive.nextEntry
                while (entry != null && count < 500) {
                    val e = entry!!
                    val sizeStr = if (e.size >= 0) formatSize(e.size) else "?"
                    val dir = if (e.isDirectory) "/" else ""
                    sb.appendLine("  $sizeStr\t${e.name}$dir")
                    entry = archive.nextEntry
                    count++
                }
                if (count >= 500) sb.appendLine("\n  ... (truncated at 500 entries)")
                archive.close()
                sb.toString()
            } ?: "[Could not read archive]"
        } catch (e: Exception) {
            "[Archive listing failed: ${e.message}]\n\nTry renaming to .zip if this is a ZIP-based format."
        }
    }

    fun save(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return cursor.getString(idx)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0) return cursor.getLong(idx)
                }
            }
        }
        return 0
    }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))}MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024 * 1024))}GB"
    }
}
