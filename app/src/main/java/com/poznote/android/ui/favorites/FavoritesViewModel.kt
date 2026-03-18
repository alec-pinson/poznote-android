package com.poznote.android.ui.favorites

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
import java.net.URLDecoder
import javax.inject.Inject

data class FavoritesUiState(
    val notes: List<NoteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workspaceName: String = URLDecoder.decode(
        checkNotNull(savedStateHandle["workspaceName"]), "UTF-8"
    )

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            noteRepository.getNotes(workspaceName, favorite = true).fold(
                onSuccess = { notes ->
                    _uiState.value = FavoritesUiState(notes = notes)
                },
                onFailure = { e ->
                    _uiState.value = FavoritesUiState(error = e.message ?: "Failed to load favorites")
                }
            )
        }
    }

    fun toggleFavorite(noteId: Int) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(noteId).onSuccess { load() }
        }
    }
}
