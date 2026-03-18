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
    val name: String,
    val created: String? = null
)

@JsonClass(generateAdapter = true)
data class FolderDto(
    val id: Int,
    val name: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    val path: String? = null,
    val icon: String? = null,
    @Json(name = "icon_color") val iconColor: String? = null
)

@JsonClass(generateAdapter = true)
data class NoteDto(
    val id: Int,
    @Json(name = "heading") val title: String = "",
    val content: String? = null,
    val type: String = "note",
    @Json(name = "folder_id") val folderId: Int? = null,
    val workspace: String = "",
    val favorite: Boolean = false,
    val tags: String = "",          // comma-separated string from API
    val updated: String? = null,
    val created: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateNoteRequest(
    @Json(name = "heading") val title: String,
    val content: String = "",
    val type: String = "markdown",
    @Json(name = "folder_id") val folderId: Int? = null,
    val workspace: String
)

@JsonClass(generateAdapter = true)
data class UpdateNoteRequest(
    @Json(name = "heading") val title: String? = null,
    val content: String? = null,
    val tags: String? = null
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
data class SearchResponse(
    val results: List<NoteDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TrashListResponse(
    val notes: List<NoteDto> = emptyList()
)
