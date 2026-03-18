package com.poznote.android.ui.notes

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.poznote.android.data.local.AuthPreferences
import com.poznote.android.data.remote.model.NoteDto
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import android.util.Base64
import android.widget.TextView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthPrefsViewerEntryPoint {
    fun authPreferences(): AuthPreferences
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerScreen(
    noteId: Int,
    onEdit: (NoteDto) -> Unit,
    onBack: () -> Unit,
    viewModel: NoteViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.note?.title?.ifBlank { "Untitled" } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.note?.let { note ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (note.favorite) Icons.Filled.Star
                                    else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (note.favorite) MaterialTheme.colorScheme.primary
                                    else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { onEdit(note) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.note != null -> {
                    NoteContent(note = uiState.note!!)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move to Trash?") },
            text = { Text("This note will be moved to trash.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteNote(onSuccess = onBack)
                }) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NoteContent(note: NoteDto) {
    when (note.type) {
        "markdown", "tasklist" -> MarkdownContent(content = note.content ?: "")
        "note" -> HtmlContent(content = note.content ?: "", noteId = note.id)
        "excalidraw" -> ExcalidrawPlaceholder()
        else -> MarkdownContent(content = note.content ?: "")
    }
}

@Composable
private fun MarkdownContent(content: String) {
    val context = LocalContext.current
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .build()
    }
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(48, 32, 48, 32)
                textSize = 15f
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, content)
        },
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
}

@Composable
private fun HtmlContent(content: String, noteId: Int) {
    val context = LocalContext.current
    val authPrefs = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthPrefsViewerEntryPoint::class.java
        ).authPreferences()
    }
    val credentials = remember(authPrefs.username, authPrefs.password) {
        Base64.encodeToString(
            "${authPrefs.username}:${authPrefs.password}".toByteArray(),
            Base64.NO_WRAP
        )
    }
    val webViewState = rememberWebViewState(url = "about:blank")

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            val html = """
                <!DOCTYPE html>
                <html><head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>body { font-family: sans-serif; padding: 16px; word-wrap: break-word; }</style>
                </head><body>$content</body></html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        client = object : AccompanistWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    view?.context?.startActivity(intent)
                }
                return true
            }
        }
    )
}

@Composable
private fun ExcalidrawPlaceholder() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Excalidraw notes cannot be rendered in-app.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = {
                // Open in browser — user can navigate there directly
            }) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open in Browser")
            }
        }
    }
}
