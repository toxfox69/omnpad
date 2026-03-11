package com.energenai.omnpad.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.Document
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    PDF("PDF", "pdf", "application/pdf"),
    DOCX("Word (DOCX)", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX("Excel (XLSX)", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    TXT("Plain Text", "txt", "text/plain"),
    HTML("HTML", "html", "text/html"),
    MD("Markdown", "md", "text/markdown"),
    CSV("CSV", "csv", "text/csv"),
    JSON("JSON", "json", "application/json"),
}

object FileConverter {

    fun getExportOptions(sourceCategory: FileCategory): List<ExportFormat> {
        return when (sourceCategory) {
            FileCategory.TEXT, FileCategory.CODE, FileCategory.MARKDOWN -> listOf(
                ExportFormat.PDF, ExportFormat.DOCX, ExportFormat.HTML,
                ExportFormat.TXT, ExportFormat.MD,
            )
            FileCategory.DATA -> listOf(
                ExportFormat.PDF, ExportFormat.DOCX, ExportFormat.TXT,
                ExportFormat.CSV, ExportFormat.JSON, ExportFormat.HTML,
            )
            FileCategory.PDF -> listOf(
                ExportFormat.DOCX, ExportFormat.TXT, ExportFormat.HTML, ExportFormat.MD,
            )
            FileCategory.OFFICE -> listOf(
                ExportFormat.PDF, ExportFormat.TXT, ExportFormat.HTML,
                ExportFormat.CSV, ExportFormat.DOCX,
            )
            else -> listOf(ExportFormat.TXT)
        }
    }

    fun convert(
        context: Context,
        content: String,
        sourceFile: LoadedFile,
        targetFormat: ExportFormat,
        outputUri: Uri,
    ): Boolean {
        return try {
            // Special case: PDF source → DOCX preserving page images
            if (sourceFile.type.category == FileCategory.PDF && targetFormat == ExportFormat.DOCX) {
                convertPdfToDocxWithImages(context, sourceFile.uri, content, outputUri)
                return true
            }
            // Special case: DOCX source → PDF preserving structure
            if (sourceFile.type.category == FileCategory.OFFICE && targetFormat == ExportFormat.PDF) {
                convertDocxToPdfWithFormatting(context, content, sourceFile.name, outputUri)
                return true
            }

            when (targetFormat) {
                ExportFormat.PDF -> writePdf(context, content, sourceFile.name, outputUri)
                ExportFormat.DOCX -> writeDocx(context, content, sourceFile.name, outputUri)
                ExportFormat.XLSX -> writeXlsx(context, content, outputUri)
                ExportFormat.HTML -> writeHtml(context, content, sourceFile.name, outputUri)
                ExportFormat.TXT, ExportFormat.MD, ExportFormat.JSON -> {
                    writeText(context, content, outputUri)
                }
                ExportFormat.CSV -> writeCsv(context, content, outputUri)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * PDF → DOCX: Renders each page as a compressed JPEG and embeds in DOCX.
     * Processes one page at a time to avoid OOM on mobile devices.
     */
    private fun convertPdfToDocxWithImages(
        context: Context,
        pdfUri: Uri,
        extractedText: String,
        outputUri: Uri,
    ) {
        val doc = XWPFDocument()

        val sectPr = doc.document.body.addNewSectPr()
        val pgMar = sectPr.addNewPgMar()
        pgMar.top = 720.toBigInteger()
        pgMar.bottom = 720.toBigInteger()
        pgMar.left = 720.toBigInteger()
        pgMar.right = 720.toBigInteger()

        val fd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return
        val renderer = PdfRenderer(fd)
        val pageCount = renderer.pageCount

        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)

            // 1x scale + JPEG to keep memory low
            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val jpegBytes = baos.toByteArray()
            val pageWidth = page.width
            val pageHeight = page.height
            bitmap.recycle()
            page.close()

            // Page header
            val headerPara = doc.createParagraph()
            headerPara.alignment = ParagraphAlignment.LEFT
            val headerRun = headerPara.createRun()
            headerRun.isBold = true
            headerRun.fontSize = 10
            headerRun.color = "666666"
            headerRun.setText("— Page ${i + 1} of $pageCount —")

            // Embed image
            val imgPara = doc.createParagraph()
            imgPara.alignment = ParagraphAlignment.CENTER
            val imgRun = imgPara.createRun()
            val imgStream = ByteArrayInputStream(jpegBytes)
            val targetWidthEmu = Units.toEMU(468.0)
            val aspectRatio = pageHeight.toDouble() / pageWidth.toDouble()
            val targetHeightEmu = (targetWidthEmu * aspectRatio).toInt()
            imgRun.addPicture(
                imgStream,
                Document.PICTURE_TYPE_JPEG,
                "page_${i + 1}.jpg",
                targetWidthEmu,
                targetHeightEmu,
            )
            imgStream.close()

            if (i < pageCount - 1) {
                val breakPara = doc.createParagraph()
                breakPara.isPageBreak = true
            }
        }

        // Append extracted text for searchability
        if (extractedText.isNotBlank()) {
            val textHeader = doc.createParagraph()
            textHeader.isPageBreak = true
            val textHeaderRun = textHeader.createRun()
            textHeaderRun.isBold = true
            textHeaderRun.fontSize = 14
            textHeaderRun.setText("Extracted Text")

            for (line in extractedText.split('\n')) {
                val para = doc.createParagraph()
                val run = para.createRun()
                run.fontSize = 10
                run.fontFamily = "Calibri"
                run.setText(line)
            }
        }

        renderer.close()
        fd.close()

        context.contentResolver.openOutputStream(outputUri)?.use { stream ->
            doc.write(stream)
        }
        doc.close()
    }

    /**
     * DOCX → PDF: Renders text with basic formatting preserved
     */
    private fun convertDocxToPdfWithFormatting(
        context: Context,
        content: String,
        title: String,
        outputUri: Uri,
    ) {
        // For now, use text-based PDF generation with better formatting
        // Full DOCX→PDF with styling would require a layout engine
        writePdf(context, content, title, outputUri)
    }

    private fun writePdf(context: Context, content: String, title: String, uri: Uri) {
        val doc = PdfDocument()
        val paint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }

        val pageWidth = 595  // A4
        val pageHeight = 842
        val marginLeft = 50f
        val marginTop = 60f
        val marginBottom = 50f
        val lineHeight = 16f
        val usableWidth = pageWidth - 100  // margins both sides
        val linesPerPage = ((pageHeight - marginTop - marginBottom) / lineHeight).toInt()

        // Split content into lines, wrapping long lines
        val wrappedLines = mutableListOf<String>()
        for (line in content.split('\n')) {
            if (line.isEmpty()) {
                wrappedLines.add("")
                continue
            }
            // Simple character-based wrapping
            val maxChars = (usableWidth / (paint.textSize * 0.5f)).toInt()
            var remaining = line
            while (remaining.length > maxChars) {
                val breakAt = remaining.lastIndexOf(' ', maxChars)
                val splitAt = if (breakAt > maxChars / 2) breakAt else maxChars
                wrappedLines.add(remaining.substring(0, splitAt))
                remaining = remaining.substring(splitAt).trimStart()
            }
            wrappedLines.add(remaining)
        }

        val totalPages = (wrappedLines.size + linesPerPage - 1) / linesPerPage
        for (pageNum in 0 until maxOf(totalPages, 1)) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            // White background
            canvas.drawColor(android.graphics.Color.WHITE)

            // Title on first page
            var y = marginTop
            if (pageNum == 0) {
                canvas.drawText(title, marginLeft, y, titlePaint)
                y += lineHeight * 2
            }

            // Content lines for this page
            val startLine = if (pageNum == 0) 0 else (pageNum * linesPerPage - 2) // account for title
            val endLine = minOf(startLine + linesPerPage, wrappedLines.size)

            for (i in startLine until endLine) {
                if (y > pageHeight - marginBottom) break
                canvas.drawText(wrappedLines[i], marginLeft, y, paint)
                y += lineHeight
            }

            // Page number
            val pageText = "Page ${pageNum + 1} of $totalPages"
            val pageTextWidth = paint.measureText(pageText)
            canvas.drawText(
                pageText,
                (pageWidth - pageTextWidth) / 2,
                pageHeight - 25f,
                Paint().apply { textSize = 9f; color = android.graphics.Color.GRAY; isAntiAlias = true }
            )

            doc.finishPage(page)
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            doc.writeTo(stream)
        }
        doc.close()
    }

    private fun writeDocx(context: Context, content: String, title: String, uri: Uri) {
        val doc = XWPFDocument()

        // Title
        val titlePara = doc.createParagraph()
        titlePara.alignment = ParagraphAlignment.LEFT
        val titleRun = titlePara.createRun()
        titleRun.isBold = true
        titleRun.fontSize = 18
        titleRun.setText(title)

        // Content — preserve paragraphs
        for (paragraph in content.split('\n')) {
            val para = doc.createParagraph()
            para.alignment = ParagraphAlignment.LEFT
            val run = para.createRun()
            run.fontSize = 11
            run.fontFamily = "Calibri"
            run.setText(paragraph)
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            doc.write(stream)
        }
        doc.close()
    }

    private fun writeXlsx(context: Context, content: String, uri: Uri) {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Sheet1")

        // Try to parse as tabular data
        val lines = content.split('\n')
        for ((rowIdx, line) in lines.withIndex()) {
            val row = sheet.createRow(rowIdx)
            // Split by tab first, then comma if no tabs found
            val cells = if (line.contains('\t')) line.split('\t')
            else line.split(',').map { it.trim() }

            for ((colIdx, cellText) in cells.withIndex()) {
                val cell = row.createCell(colIdx)
                // Try to parse as number
                val num = cellText.toDoubleOrNull()
                if (num != null) {
                    cell.setCellValue(num)
                } else {
                    cell.setCellValue(cellText)
                }
            }
        }

        // Auto-size columns (up to 10)
        for (i in 0 until minOf(10, sheet.getRow(0)?.lastCellNum?.toInt() ?: 0)) {
            sheet.autoSizeColumn(i)
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            wb.write(stream)
        }
        wb.close()
    }

    private fun writeHtml(context: Context, content: String, title: String, uri: Uri) {
        val escaped = content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br>\n")

        val html = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$title</title>
<style>
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; max-width: 800px; margin: 2rem auto; padding: 0 1rem; line-height: 1.6; color: #222; }
h1 { border-bottom: 2px solid #00FFD1; padding-bottom: 0.5rem; }
pre { background: #f5f5f5; padding: 1rem; border-radius: 4px; overflow-x: auto; font-size: 14px; }
</style>
</head>
<body>
<h1>$title</h1>
<pre>$escaped</pre>
</body>
</html>"""

        writeText(context, html, uri)
    }

    private fun writeCsv(context: Context, content: String, uri: Uri) {
        // If content has tabs, convert to comma-separated
        val csv = if (content.contains('\t')) {
            content.split('\n').joinToString("\n") { line ->
                line.split('\t').joinToString(",") { cell ->
                    if (cell.contains(',') || cell.contains('"') || cell.contains('\n')) {
                        "\"${cell.replace("\"", "\"\"")}\""
                    } else cell
                }
            }
        } else content

        writeText(context, csv, uri)
    }

    private fun writeText(context: Context, content: String, uri: Uri) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
            stream.flush()
        }
    }

    fun suggestFileName(originalName: String, targetFormat: ExportFormat): String {
        val baseName = originalName.substringBeforeLast('.')
        return "$baseName.${targetFormat.extension}"
    }
}
