package com.poznote.android.ui.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poznote.android.data.remote.model.FolderDto
import com.poznote.android.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderBrowserUiState(
    val folders: List<FolderDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FolderBrowserViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workspaceId: Int = checkNotNull(savedStateHandle["workspaceId"])

    private val _uiState = MutableStateFlow(FolderBrowserUiState())
    val uiState: StateFlow<FolderBrowserUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            folderRepository.getFolders(workspaceId).fold(
                onSuccess = { folders ->
                    _uiState.value = FolderBrowserUiState(folders = folders)
                },
                onFailure = { e ->
                    _uiState.value = FolderBrowserUiState(error = e.message ?: "Failed to load folders")
                }
            )
        }
    }
}
