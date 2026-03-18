package com.poznote.android.ui.workspaces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.poznote.android.data.remote.model.WorkspaceDto
import com.poznote.android.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspacesScreen(
    onWorkspaceClick: (WorkspaceDto) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: (Int) -> Unit,
    onTrashClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: WorkspacesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspaces") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Search") },
                            onClick = {
                                menuExpanded = false
                                onSearchClick()
                            }
                        )
                        uiState.workspaces.firstOrNull()?.let { ws ->
                            DropdownMenuItem(
                                text = { Text("Favorites") },
                                onClick = {
                                    menuExpanded = false
                                    onFavoritesClick(ws.id)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Trash") },
                            onClick = {
                                menuExpanded = false
                                onTrashClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                menuExpanded = false
                                viewModel.logout()
                                onLogout()
                            }
                        )
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
                uiState.workspaces.isEmpty() && !uiState.isLoading ->
                    EmptyState(message = "No workspaces found")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.workspaces) { workspace ->
                        WorkspaceCard(
                            workspace = workspace,
                            onClick = { onWorkspaceClick(workspace) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceCard(workspace: WorkspaceDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workspace.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (workspace.noteCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workspace.noteCount} notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
