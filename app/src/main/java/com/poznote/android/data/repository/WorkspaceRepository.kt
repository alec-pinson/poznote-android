package com.poznote.android.data.repository

import com.poznote.android.data.remote.api.PoznoteApi
import com.poznote.android.data.remote.model.WorkspaceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepository @Inject constructor(
    private val api: PoznoteApi
) {
    suspend fun getWorkspaces(): Result<List<WorkspaceDto>> = runCatching {
        api.getWorkspaces().workspaces
    }
}
