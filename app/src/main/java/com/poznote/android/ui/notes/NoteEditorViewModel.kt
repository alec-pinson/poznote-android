package com.poznote.android.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditorUiState(
    val noteId: Int? = null,
    val title: String = "",
    val content: String = "",
    val type: String = "markdown",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showPreview: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workspaceId: Int = checkNotNull(savedStateHandle["workspaceId"])
    private val noteIdArg: Int? = savedStateHandle.get<Int>("noteId")?.takeIf { it != -1 }
    private val noteTypeArg: String = savedStateHandle.get<String>("noteType") ?: "markdown"
    private val folderIdArg: Int? = savedStateHandle.get<Int>("folderId")?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow(
        NoteEditorUiState(
            noteId = noteIdArg,
            type = noteTypeArg
        )
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        if (noteIdArg != null) {
            loadNote(noteIdArg)
        }
    }

    private fun loadNote(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            noteRepository.getNote(id).fold(
                onSuccess = { note ->
                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        content = note.content ?: "",
                        type = note.type,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load note"
                    )
                }
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
        scheduleAutoSave()
    }

    fun onContentChange(value: String) {
        _uiState.value = _uiState.value.copy(content = value)
        scheduleAutoSave()
    }

    fun togglePreview() {
        _uiState.value = _uiState.value.copy(showPreview = !_uiState.value.showPreview)
    }

    private fun scheduleAutoSave() {
        val state = _uiState.value
        if (state.noteId == null) return // New notes need explicit save first
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000L)
            save(state.noteId)
        }
    }

    fun save(noteId: Int? = _uiState.value.noteId, onSuccess: ((NoteDto) -> Unit)? = null) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            if (noteId != null) {
                // Update existing
                noteRepository.updateNote(
                    id = noteId,
                    title = state.title,
                    content = state.content
                ).fold(
                    onSuccess = { updated ->
                        _uiState.value = _uiState.value.copy(
                            noteId = updated.id,
                            isSaving = false,
                            error = null
                        )
                        onSuccess?.invoke(updated)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = e.message ?: "Save failed"
                        )
                    }
                )
            } else {
                // Create new
                noteRepository.createNote(
                    title = state.title.ifBlank { "Untitled" },
                    content = state.content,
                    type = state.type,
                    workspaceId = workspaceId,
                    folderId = folderIdArg
                ).fold(
                    onSuccess = { created ->
                        _uiState.value = _uiState.value.copy(
                            noteId = created.id,
                            title = created.title,
                            isSaving = false,
                            error = null
                        )
                        onSuccess?.invoke(created)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to create note"
                        )
                    }
                )
            }
        }
    }
}
