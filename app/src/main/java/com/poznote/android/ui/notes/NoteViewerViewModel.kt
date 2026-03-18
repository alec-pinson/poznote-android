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

data class NoteViewerUiState(
    val note: NoteDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteViewerViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Int = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(NoteViewerUiState())
    val uiState: StateFlow<NoteViewerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = NoteViewerUiState(isLoading = true)
            noteRepository.getNote(noteId).fold(
                onSuccess = { note ->
                    _uiState.value = NoteViewerUiState(note = note)
                },
                onFailure = { e ->
                    _uiState.value = NoteViewerUiState(error = e.message ?: "Failed to load note")
                }
            )
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.value.note?.let { note ->
                noteRepository.toggleFavorite(note.id).onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(note = updated)
                }
            }
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value.note?.let { note ->
                noteRepository.deleteNote(note.id).onSuccess {
                    onSuccess()
                }
            }
        }
    }
}
