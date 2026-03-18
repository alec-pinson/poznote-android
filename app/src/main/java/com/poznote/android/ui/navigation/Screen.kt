package com.poznote.android.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Workspaces : Screen("workspaces")
    object FolderBrowser : Screen("folders/{workspaceId}/{workspaceName}") {
        fun createRoute(workspaceId: Int, workspaceName: String) =
            "folders/$workspaceId/${workspaceName.encodeForRoute()}"
    }
    object NoteList : Screen("notes/{workspaceId}/{folderId}/{folderName}") {
        fun createRoute(workspaceId: Int, folderId: Int?, folderName: String) =
            "notes/$workspaceId/${folderId ?: -1}/${folderName.encodeForRoute()}"
    }
    object NoteViewer : Screen("note/{noteId}") {
        fun createRoute(noteId: Int) = "note/$noteId"
    }
    object NoteEditor : Screen("editor/{workspaceId}/{noteId}/{noteType}") {
        fun createRoute(workspaceId: Int, noteId: Int, noteType: String) =
            "editor/$workspaceId/$noteId/${noteType.encodeForRoute()}"
        fun createNewRoute(workspaceId: Int, folderId: Int?) =
            "editor/$workspaceId/-1/markdown?folderId=${folderId ?: -1}"
    }
    object Search : Screen("search")
    object Favorites : Screen("favorites/{workspaceId}") {
        fun createRoute(workspaceId: Int) = "favorites/$workspaceId"
    }
    object Trash : Screen("trash")
}

private fun String.encodeForRoute(): String =
    java.net.URLEncoder.encode(this, "UTF-8")
