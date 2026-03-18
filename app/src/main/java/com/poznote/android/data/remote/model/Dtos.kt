package com.poznote.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val username: String,
    val email: String? = null
)

@JsonClass(generateAdapter = true)
data class WorkspaceDto(
    val id: Int,
    val name: String,
    @Json(name = "note_count") val noteCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class FolderDto(
    val id: Int,
    val name: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    @Json(name = "workspace_id") val workspaceId: Int = 0,
    val depth: Int = 0
)

@JsonClass(generateAdapter = true)
data class NoteDto(
    val id: Int,
    val title: String,
    val content: String? = null,
    val type: String = "markdown",
    @Json(name = "folder_id") val folderId: Int? = null,
    @Json(name = "workspace_id") val workspaceId: Int = 0,
    @Json(name = "is_favorite") val isFavorite: Boolean = false,
    @Json(name = "is_trashed") val isTrashed: Boolean = false,
    val tags: List<String> = emptyList(),
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateNoteRequest(
    val title: String,
    val content: String = "",
    val type: String = "markdown",
    @Json(name = "folder_id") val folderId: Int? = null,
    @Json(name = "workspace_id") val workspaceId: Int
)

@JsonClass(generateAdapter = true)
data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class WorkspaceListResponse(
    val workspaces: List<WorkspaceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FolderListResponse(
    val folders: List<FolderDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NoteListResponse(
    val notes: List<NoteDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TrashListResponse(
    val notes: List<NoteDto> = emptyList()
)
