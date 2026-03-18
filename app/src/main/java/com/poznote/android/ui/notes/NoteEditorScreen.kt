package com.poznote.android.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    workspaceName: String,
    noteId: Int?,
    noteType: String,
    folderId: Int?,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            if (uiState.title.isEmpty()) {
                                Text("Title", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            inner()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    if (noteType in listOf("markdown", "tasklist")) {
                        IconButton(onClick = viewModel::togglePreview) {
                            Icon(
                                imageVector = if (uiState.showPreview) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                contentDescription = "Toggle preview"
                            )
                        }
                    }
                    if (uiState.noteId == null) {
                        IconButton(onClick = { viewModel.save(onSuccess = { onBack() }) }) {
                            Icon(Icons.Filled.Save, contentDescription = "Save")
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
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.showPreview && noteType in listOf("markdown", "tasklist") -> {
                    MarkdownPreview(content = uiState.content)
                }
                else -> ContentEditor(
                    content = uiState.content,
                    onContentChange = viewModel::onContentChange
                )
            }
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun ContentEditor(content: String, onContentChange: (String) -> Unit) {
    BasicTextField(
        value = content,
        onValueChange = onContentChange,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    )
}

@Composable
private fun MarkdownPreview(content: String) {
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
