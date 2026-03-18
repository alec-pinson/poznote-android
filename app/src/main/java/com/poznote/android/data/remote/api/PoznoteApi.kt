package com.poznote.android.data.remote.api

import com.poznote.android.data.remote.model.*
import retrofit2.http.*

interface PoznoteApi {

    @GET("api/v1/users/me")
    suspend fun getMe(): UserDto

    @GET("api/v1/workspaces")
    suspend fun getWorkspaces(): WorkspaceListResponse

    @GET("api/v1/folders")
    suspend fun getFolders(
        @Query("workspace") workspaceId: Int,
        @Query("tree") tree: Boolean = false
    ): FolderListResponse

    @GET("api/v1/notes")
    suspend fun getNotes(
        @Query("workspace") workspaceId: Int,
        @Query("folder_id") folderId: Int? = null,
        @Query("favorite") favorite: Int? = null
    ): NoteListResponse

    @GET("api/v1/notes/{id}")
    suspend fun getNote(@Path("id") id: Int): NoteDto

    @POST("api/v1/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): NoteDto

    @PATCH("api/v1/notes/{id}")
    suspend fun updateNote(
        @Path("id") id: Int,
        @Body request: UpdateNoteRequest
    ): NoteDto

    @DELETE("api/v1/notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int)

    @POST("api/v1/notes/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") id: Int): NoteDto

    @GET("api/v1/notes/search")
    suspend fun searchNotes(@Query("q") query: String): NoteListResponse

    @GET("api/v1/trash")
    suspend fun getTrash(): TrashListResponse

    @POST("api/v1/notes/{id}/restore")
    suspend fun restoreNote(@Path("id") id: Int): NoteDto

    @DELETE("api/v1/trash/{id}")
    suspend fun permanentlyDeleteNote(@Path("id") id: Int)
}
