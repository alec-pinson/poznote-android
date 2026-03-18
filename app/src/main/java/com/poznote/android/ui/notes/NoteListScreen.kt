package com.poznote.android.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.ui.components.EmptyState
import com.poznote.android.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    workspaceId: Int,
    folderId: Int?,
    folderName: String,
    onNoteClick: (Int) -> Unit,
    onCreateNote: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var contextMenuNote by remember { mutableStateOf<NoteDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(Icons.Filled.Add, contentDescription = "Create note")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::load,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.error != null -> EmptyState(message = uiState.error!!)
                uiState.notes.isEmpty() && !uiState.isLoading -> EmptyState(message = "No notes here")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onLongClick = { contextMenuNote = note }
                        )
                    }
                }
            }
        }
    }

    contextMenuNote?.let { note ->
        NoteContextMenu(
            note = note,
            onDismiss = { contextMenuNote = null },
            onFavorite = {
                viewModel.toggleFavorite(note.id)
                contextMenuNote = null
            },
            onTrash = {
                viewModel.deleteNote(note.id)
                contextMenuNote = null
            }
        )
    }
}

@Composable
private fun NoteContextMenu(
    note: NoteDto,
    onDismiss: () -> Unit,
    onFavorite: () -> Unit,
    onTrash: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(note.title.ifBlank { "Untitled" }) },
        text = {
            Column {
                TextButton(onClick = onFavorite, modifier = Modifier.fillMaxWidth()) {
                    Text(if (note.isFavorite) "Remove from Favorites" else "Add to Favorites")
                }
                TextButton(onClick = onTrash, modifier = Modifier.fillMaxWidth()) {
                    Text("Move to Trash")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
