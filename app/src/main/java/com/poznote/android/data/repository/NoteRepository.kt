package com.poznote.android.data.repository

import com.poznote.android.data.remote.api.PoznoteApi
import com.poznote.android.data.remote.model.CreateNoteRequest
import com.poznote.android.data.remote.model.NoteDto
import com.poznote.android.data.remote.model.UpdateNoteRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val api: PoznoteApi
) {
    suspend fun getNotes(
        workspaceName: String,
        folderId: Int? = null,
        favorite: Boolean? = null
    ): Result<List<NoteDto>> = runCatching {
        api.getNotes(
            workspaceName = workspaceName,
            folderId = folderId,
            favorite = if (favorite == true) 1 else null
        ).notes
    }

    suspend fun getNote(id: Int): Result<NoteDto> = runCatching {
        api.getNote(id)
    }

    suspend fun createNote(
        title: String,
        content: String,
        type: String,
        workspaceName: String,
        folderId: Int? = null
    ): Result<NoteDto> = runCatching {
        api.createNote(
            CreateNoteRequest(
                title = title,
                content = content,
                type = type,
                workspace = workspaceName,
                folderId = folderId
            )
        )
    }

    suspend fun updateNote(
        id: Int,
        title: String? = null,
        content: String? = null,
        tags: String? = null
    ): Result<NoteDto> = runCatching {
        api.updateNote(id, UpdateNoteRequest(title = title, content = content, tags = tags))
    }

    suspend fun deleteNote(id: Int): Result<Unit> = runCatching {
        api.deleteNote(id)
    }

    suspend fun toggleFavorite(id: Int): Result<NoteDto> = runCatching {
        api.toggleFavorite(id)
    }

    suspend fun searchNotes(query: String): Result<List<NoteDto>> = runCatching {
        api.searchNotes(query).results
    }

    suspend fun getTrash(): Result<List<NoteDto>> = runCatching {
        api.getTrash().notes
    }

    suspend fun restoreNote(id: Int): Result<NoteDto> = runCatching {
        api.restoreNote(id)
    }

    suspend fun permanentlyDeleteNote(id: Int): Result<Unit> = runCatching {
        api.permanentlyDeleteNote(id)
    }
}
