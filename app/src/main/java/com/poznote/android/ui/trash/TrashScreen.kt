package com.poznote.android.ui.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.notes.isNotEmpty()) {
                        TextButton(onClick = { showEmptyTrashDialog = true }) {
                            Text("Empty Trash")
                        }
                    }
                }
            )
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
                uiState.notes.isEmpty() && !uiState.isLoading ->
                    EmptyState(message = "Trash is empty")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        TrashNoteItem(
                            note = note,
                            onRestore = { viewModel.restoreNote(note.id) },
                            onDelete = { viewModel.permanentlyDelete(note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?") },
            text = { Text("This will permanently delete all ${uiState.notes.size} notes in trash. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmptyTrashDialog = false
                        viewModel.emptyTrash()
                    }
                ) {
                    Text("Empty Trash", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TrashNoteItem(
    note: NoteDto,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleSmall
                )
                note.updated?.let {
                    Text(
                        text = it.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.Filled.RestoreFromTrash,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.DeleteForever,
                        contentDescription = "Delete permanently",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
