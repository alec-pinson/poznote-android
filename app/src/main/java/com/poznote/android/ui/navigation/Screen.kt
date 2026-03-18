package com.poznote.android.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Workspaces : Screen("workspaces")
    object FolderBrowser : Screen("folders/{workspaceName}") {
        fun createRoute(workspaceName: String) =
            "folders/${workspaceName.encodeForRoute()}"
    }
    object NoteList : Screen("notes/{workspaceName}/{folderId}/{folderName}") {
        fun createRoute(workspaceName: String, folderId: Int?, folderName: String) =
            "notes/${workspaceName.encodeForRoute()}/${folderId ?: -1}/${folderName.encodeForRoute()}"
    }
    object NoteViewer : Screen("note/{noteId}") {
        fun createRoute(noteId: Int) = "note/$noteId"
    }
    object NoteEditor : Screen("editor/{workspaceName}/{noteId}/{noteType}") {
        fun createRoute(workspaceName: String, noteId: Int, noteType: String) =
            "editor/${workspaceName.encodeForRoute()}/$noteId/${noteType.encodeForRoute()}"
        fun createNewRoute(workspaceName: String, folderId: Int?) =
            "editor/${workspaceName.encodeForRoute()}/-1/markdown?folderId=${folderId ?: -1}"
    }
    object Search : Screen("search")
    object Favorites : Screen("favorites/{workspaceName}") {
        fun createRoute(workspaceName: String) = "favorites/${workspaceName.encodeForRoute()}"
    }
    object Trash : Screen("trash")
}

private fun String.encodeForRoute(): String =
    java.net.URLEncoder.encode(this, "UTF-8")
