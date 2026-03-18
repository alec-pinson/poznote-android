package com.poznote.android.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val notes: List<NoteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workspaceId: Int = checkNotNull(savedStateHandle["workspaceId"])
    private val folderIdRaw: Int = savedStateHandle["folderId"] ?: -1
    private val folderId: Int? = if (folderIdRaw == -1) null else folderIdRaw

    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            noteRepository.getNotes(workspaceId, folderId).fold(
                onSuccess = { notes ->
                    _uiState.value = NoteListUiState(notes = notes)
                },
                onFailure = { e ->
                    _uiState.value = NoteListUiState(error = e.message ?: "Failed to load notes")
                }
            )
        }
    }

    fun toggleFavorite(noteId: Int) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(noteId).onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    notes = _uiState.value.notes.map { if (it.id == noteId) updated else it }
                )
            }
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    notes = _uiState.value.notes.filter { it.id != noteId }
                )
            }
        }
    }
}
