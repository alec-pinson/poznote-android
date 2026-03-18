package com.poznote.android.data.repository

import com.poznote.android.data.remote.api.PoznoteApi
import com.poznote.android.data.remote.model.FolderDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val api: PoznoteApi
) {
    suspend fun getFolders(workspaceName: String): Result<List<FolderDto>> = runCatching {
        api.getFolders(workspaceName = workspaceName).folders
    }
}
