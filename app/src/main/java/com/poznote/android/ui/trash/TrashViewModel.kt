package com.poznote.android.ui.trash

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

data class TrashUiState(
    val notes: List<NoteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            noteRepository.getTrash().fold(
                onSuccess = { notes ->
                    _uiState.value = TrashUiState(notes = notes)
                },
                onFailure = { e ->
                    _uiState.value = TrashUiState(error = e.message ?: "Failed to load trash")
                }
            )
        }
    }

    fun restoreNote(noteId: Int) {
        viewModelScope.launch {
            noteRepository.restoreNote(noteId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    notes = _uiState.value.notes.filter { it.id != noteId }
                )
            }
        }
    }

    fun permanentlyDelete(noteId: Int) {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteNote(noteId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    notes = _uiState.value.notes.filter { it.id != noteId }
                )
            }
        }
    }

    fun emptyTrash() {
        val noteIds = _uiState.value.notes.map { it.id }
        viewModelScope.launch {
            noteIds.forEach { id ->
                noteRepository.permanentlyDeleteNote(id)
            }
            _uiState.value = _uiState.value.copy(notes = emptyList())
        }
    }
}
