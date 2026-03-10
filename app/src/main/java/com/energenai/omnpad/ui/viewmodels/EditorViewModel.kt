package com.energenai.omnpad.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energenai.omnpad.data.FileLoader
import com.energenai.omnpad.data.LoadedFile
import kotlinx.coroutines.launch

data class Tab(
    val file: LoadedFile,
    val modified: Boolean = false,
    val content: String = file.textContent ?: "",
)

class EditorViewModel : ViewModel() {
    val tabs = mutableStateListOf<Tab>()
    val activeTabIndex = mutableStateOf(-1)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val searchQuery = mutableStateOf("")
    val replaceText = mutableStateOf("")
    val showSearch = mutableStateOf(false)
    val wordWrap = mutableStateOf(true)
    val showLineNumbers = mutableStateOf(true)

    val activeTab: Tab? get() = tabs.getOrNull(activeTabIndex.value)

    fun openFile(context: Context, uri: Uri) {
        // Check if already open
        val existing = tabs.indexOfFirst { it.file.uri == uri }
        if (existing >= 0) {
            activeTabIndex.value = existing
            return
        }

        isLoading.value = true
        error.value = null
        viewModelScope.launch {
            try {
                val loaded = FileLoader.load(context, uri)
                val tab = Tab(file = loaded, content = loaded.textContent ?: "")
                tabs.add(tab)
                activeTabIndex.value = tabs.size - 1
            } catch (e: Exception) {
                error.value = "Failed to open file: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateContent(content: String) {
        val idx = activeTabIndex.value
        if (idx in tabs.indices) {
            tabs[idx] = tabs[idx].copy(content = content, modified = true)
        }
    }

    fun saveFile(context: Context) {
        val tab = activeTab ?: return
        if (!tab.file.isEditable) return
        viewModelScope.launch {
            val success = FileLoader.save(context, tab.file.uri, tab.content)
            if (success) {
                val idx = activeTabIndex.value
                tabs[idx] = tabs[idx].copy(modified = false)
            } else {
                error.value = "Failed to save file"
            }
        }
    }

    fun closeTab(index: Int) {
        if (index in tabs.indices) {
            tabs.removeAt(index)
            when {
                tabs.isEmpty() -> activeTabIndex.value = -1
                activeTabIndex.value >= tabs.size -> activeTabIndex.value = tabs.size - 1
                activeTabIndex.value > index -> activeTabIndex.value--
            }
        }
    }

    fun findNext(): IntRange? {
        val query = searchQuery.value
        if (query.isEmpty()) return null
        val content = activeTab?.content ?: return null
        val idx = content.indexOf(query, ignoreCase = true)
        return if (idx >= 0) idx until idx + query.length else null
    }

    fun replaceAll() {
        val query = searchQuery.value
        val replacement = replaceText.value
        if (query.isEmpty()) return
        val content = activeTab?.content ?: return
        updateContent(content.replace(query, replacement, ignoreCase = true))
    }
}
