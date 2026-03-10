package com.energenai.omnpad.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.energenai.omnpad.data.FileCategory
import com.energenai.omnpad.data.FileConverter
import com.energenai.omnpad.data.ExportFormat
import com.energenai.omnpad.data.FileLoader
import com.energenai.omnpad.ui.components.CodeEditor
import com.energenai.omnpad.ui.components.HexViewer
import com.energenai.omnpad.ui.theme.*
import com.energenai.omnpad.ui.viewmodels.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: EditorViewModel = viewModel(),
    initialUri: Uri? = null,
) {
    val context = LocalContext.current

    // Open file from intent
    LaunchedEffect(initialUri) {
        if (initialUri != null) vm.openFile(context, initialUri)
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.openFile(context, it) } }

    var showExportMenu by remember { mutableStateOf(false) }
    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    val saveAs = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            val tab = vm.activeTab ?: return@rememberLauncherForActivityResult
            val fmt = pendingExportFormat
            if (fmt != null) {
                FileConverter.convert(context, tab.content, tab.file, fmt, uri)
                pendingExportFormat = null
            } else {
                FileLoader.save(context, uri, tab.content)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
    ) {
        // Top bar
        TopAppBar(
            title = {
                val tab = vm.activeTab
                if (tab != null) {
                    Column {
                        Text(
                            text = (if (tab.modified) "● " else "") + tab.file.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${tab.file.type.category.name} · ${FileLoader.formatSize(tab.file.size)}${tab.file.type.language?.let { " · $it" } ?: ""}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                } else {
                    Text("OmniPad", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Surface,
                titleContentColor = TextPrimary,
            ),
            actions = {
                // Search toggle
                IconButton(onClick = { vm.showSearch.value = !vm.showSearch.value }) {
                    Icon(Icons.Default.Search, "Search", tint = TextSecondary)
                }
                // Word wrap toggle
                IconButton(onClick = { vm.wordWrap.value = !vm.wordWrap.value }) {
                    Icon(
                        Icons.Default.WrapText, "Word Wrap",
                        tint = if (vm.wordWrap.value) Accent else TextSecondary
                    )
                }
                // Save
                if (vm.activeTab?.file?.isEditable == true) {
                    IconButton(onClick = { vm.saveFile(context) }) {
                        Icon(Icons.Default.Save, "Save", tint = Accent)
                    }
                }
                // Export / Convert
                if (vm.activeTab != null) {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.ImportExport, "Export / Convert", tint = Cyan)
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                            containerColor = SurfaceVariant,
                        ) {
                            val tab = vm.activeTab
                            if (tab != null) {
                                val options = FileConverter.getExportOptions(tab.file.type.category)
                                Text(
                                    "Export as...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                                options.forEach { fmt ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${fmt.label} (.${fmt.extension})",
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                            )
                                        },
                                        onClick = {
                                            showExportMenu = false
                                            pendingExportFormat = fmt
                                            val suggestedName = FileConverter.suggestFileName(
                                                tab.file.name, fmt
                                            )
                                            saveAs.launch(suggestedName)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                when (fmt) {
                                                    ExportFormat.PDF -> Icons.Default.PictureAsPdf
                                                    ExportFormat.DOCX -> Icons.Default.Article
                                                    ExportFormat.XLSX -> Icons.Default.TableChart
                                                    ExportFormat.HTML -> Icons.Default.Code
                                                    ExportFormat.CSV -> Icons.Default.GridOn
                                                    else -> Icons.Default.Description
                                                },
                                                contentDescription = null,
                                                tint = when (fmt) {
                                                    ExportFormat.PDF -> Magenta
                                                    ExportFormat.DOCX -> Cyan
                                                    ExportFormat.XLSX -> Accent
                                                    ExportFormat.HTML -> Amber
                                                    else -> TextSecondary
                                                },
                                                modifier = Modifier.size(18.dp),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                // Save As
                if (vm.activeTab != null) {
                    IconButton(onClick = {
                        pendingExportFormat = null
                        saveAs.launch(vm.activeTab?.file?.name ?: "untitled.txt")
                    }) {
                        Icon(Icons.Default.SaveAs, "Save As", tint = TextSecondary)
                    }
                }
                // Open file
                IconButton(onClick = { filePicker.launch(arrayOf("*/*")) }) {
                    Icon(Icons.Default.FolderOpen, "Open", tint = Accent)
                }
            }
        )

        // Search bar
        AnimatedVisibility(visible = vm.showSearch.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = vm.searchQuery.value,
                    onValueChange = { vm.searchQuery.value = it },
                    placeholder = { Text("Find", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = vm.replaceText.value,
                    onValueChange = { vm.replaceText.value = it },
                    placeholder = { Text("Replace", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Magenta,
                        unfocusedBorderColor = Border,
                    ),
                )
                IconButton(onClick = { vm.replaceAll() }) {
                    Icon(Icons.Default.FindReplace, "Replace All", tint = Magenta)
                }
                IconButton(onClick = { vm.showSearch.value = false }) {
                    Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                }
            }
        }

        // Tabs
        if (vm.tabs.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .padding(vertical = 2.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                itemsIndexed(vm.tabs) { index, tab ->
                    val isActive = index == vm.activeTabIndex.value
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) SurfaceVariant else Surface)
                            .clickable { vm.activeTabIndex.value = index }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = fileIcon(tab.file.type.category),
                            contentDescription = null,
                            tint = if (isActive) fileColor(tab.file.type.category) else TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = (if (tab.modified) "● " else "") + tab.file.name,
                            fontSize = 12.sp,
                            color = if (isActive) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close tab",
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { vm.closeTab(index) },
                        )
                    }
                }
            }
        }

        // Error banner
        vm.error.value?.let { err ->
            Text(
                text = err,
                color = ErrorRed,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRed.copy(alpha = 0.1f))
                    .padding(12.dp),
            )
        }

        // Loading
        if (vm.isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
            return
        }

        // Content area
        val tab = vm.activeTab
        if (tab == null) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Open a file to get started", color = TextSecondary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Supports 100+ file types",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(24.dp))
                    FilledTonalButton(
                        onClick = { filePicker.launch(arrayOf("*/*")) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Accent.copy(alpha = 0.15f),
                            contentColor = Accent,
                        ),
                    ) {
                        Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open File")
                    }
                }
            }
            return
        }

        // Render content based on file type
        when (tab.file.type.category) {
            FileCategory.TEXT, FileCategory.CODE, FileCategory.DATA, FileCategory.MARKDOWN -> {
                CodeEditor(
                    content = tab.content,
                    language = tab.file.type.language,
                    onContentChange = { vm.updateContent(it) },
                    readOnly = false,
                    showLineNumbers = vm.showLineNumbers.value,
                    wordWrap = vm.wordWrap.value,
                )
            }
            FileCategory.PDF -> {
                // Show extracted text (editable for copy/search)
                CodeEditor(
                    content = tab.content,
                    language = null,
                    onContentChange = {},
                    readOnly = true,
                    showLineNumbers = false,
                    wordWrap = true,
                )
            }
            FileCategory.OFFICE -> {
                CodeEditor(
                    content = tab.content,
                    language = null,
                    onContentChange = {},
                    readOnly = true,
                    showLineNumbers = false,
                    wordWrap = true,
                )
            }
            FileCategory.IMAGE -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = tab.file.uri,
                        contentDescription = tab.file.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }
            FileCategory.AUDIO, FileCategory.VIDEO -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (tab.file.type.category == FileCategory.AUDIO) Icons.Default.AudioFile
                            else Icons.Default.VideoFile,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(tab.file.name, color = TextPrimary, fontSize = 18.sp)
                        Text(
                            FileLoader.formatSize(tab.file.size),
                            color = TextSecondary,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Use system player for playback",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            FileCategory.ARCHIVE -> {
                CodeEditor(
                    content = tab.content,
                    language = null,
                    onContentChange = {},
                    readOnly = true,
                    showLineNumbers = true,
                    wordWrap = false,
                )
            }
            FileCategory.BINARY -> {
                if (tab.file.rawBytes != null) {
                    HexViewer(bytes = tab.file.rawBytes)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Binary file — no preview available", color = TextSecondary)
                    }
                }
            }
        }
    }
}

private fun fileIcon(category: FileCategory) = when (category) {
    FileCategory.TEXT -> Icons.Default.Description
    FileCategory.CODE -> Icons.Default.Code
    FileCategory.MARKDOWN -> Icons.Default.Article
    FileCategory.DATA -> Icons.Default.DataObject
    FileCategory.PDF -> Icons.Default.PictureAsPdf
    FileCategory.OFFICE -> Icons.Default.Article
    FileCategory.IMAGE -> Icons.Default.Image
    FileCategory.AUDIO -> Icons.Default.AudioFile
    FileCategory.VIDEO -> Icons.Default.VideoFile
    FileCategory.ARCHIVE -> Icons.Default.FolderZip
    FileCategory.BINARY -> Icons.Default.Memory
}

private fun fileColor(category: FileCategory) = when (category) {
    FileCategory.CODE -> Accent
    FileCategory.MARKDOWN -> Cyan
    FileCategory.DATA -> Amber
    FileCategory.PDF -> Magenta
    FileCategory.IMAGE -> SynString
    FileCategory.ARCHIVE -> SynNumber
    else -> TextPrimary
}
