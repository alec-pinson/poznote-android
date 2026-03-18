package com.poznote.android.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.poznote.android.data.remote.model.FolderDto
import com.poznote.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserScreen(
    workspaceId: Int,
    workspaceName: String,
    onFolderClick: (FolderDto) -> Unit,
    onAllNotesClick: () -> Unit,
    onCreateNote: () -> Unit,
    onBack: () -> Unit,
    viewModel: FolderBrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workspaceName) },
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
                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        ListItem(
                            headlineContent = { Text("All Notes") },
                            leadingContent = {
                                Icon(Icons.Filled.Notes, contentDescription = null)
                            },
                            modifier = Modifier.clickable(onClick = onAllNotesClick)
                        )
                        HorizontalDivider()
                    }
                    items(uiState.folders) { folder ->
                        FolderItem(
                            folder = folder,
                            onClick = { onFolderClick(folder) }
                        )
                    }
                    if (uiState.folders.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No folders",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderItem(folder: FolderDto, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(folder.name) },
        leadingContent = {
            Box(modifier = Modifier.padding(start = (folder.depth * 16).dp)) {
                Icon(Icons.Filled.Folder, contentDescription = null)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider(modifier = Modifier.padding(start = (16 + folder.depth * 16).dp))
}
