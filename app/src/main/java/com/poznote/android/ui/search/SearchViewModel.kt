package com.poznote.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<NoteDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300L)
                .filter { it.length >= 2 }
                .flatMapLatest { q ->
                    flow {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        noteRepository.searchNotes(q).fold(
                            onSuccess = { results ->
                                emit(SearchUiState(query = q, results = results))
                            },
                            onFailure = { e ->
                                emit(SearchUiState(query = q, error = e.message ?: "Search failed"))
                            }
                        )
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
        _uiState.value = _uiState.value.copy(query = value)
        if (value.length < 2) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
        }
    }
}
