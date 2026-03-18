package com.poznote.android.ui.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poznote.android.data.remote.model.WorkspaceDto
import com.poznote.android.data.repository.AuthRepository
import com.poznote.android.data.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkspacesUiState(
    val workspaces: List<WorkspaceDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkspacesViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspacesUiState())
    val uiState: StateFlow<WorkspacesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            workspaceRepository.getWorkspaces().fold(
                onSuccess = { workspaces ->
                    _uiState.value = WorkspacesUiState(workspaces = workspaces)
                },
                onFailure = { e ->
                    _uiState.value = WorkspacesUiState(error = e.message ?: "Failed to load workspaces")
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
