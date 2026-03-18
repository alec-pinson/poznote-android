package com.poznote.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.poznote.android.data.local.AuthPreferences
import com.poznote.android.ui.auth.LoginScreen
import com.poznote.android.ui.favorites.FavoritesScreen
import com.poznote.android.ui.folders.FolderBrowserScreen
import com.poznote.android.ui.notes.NoteEditorScreen
import com.poznote.android.ui.notes.NoteListScreen
import com.poznote.android.ui.notes.NoteViewerScreen
import com.poznote.android.ui.search.SearchScreen
import com.poznote.android.ui.trash.TrashScreen
import com.poznote.android.ui.workspaces.WorkspacesScreen
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthPrefsEntryPoint {
    fun authPreferences(): AuthPreferences
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authPrefs = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthPrefsEntryPoint::class.java
        ).authPreferences()
    }
    val startDestination = if (authPrefs.isLoggedIn()) Screen.Workspaces.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Workspaces.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Workspaces.route) {
            WorkspacesScreen(
                onWorkspaceClick = { workspace ->
                    navController.navigate(
                        Screen.FolderBrowser.createRoute(workspace.id, workspace.name)
                    )
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onFavoritesClick = { workspaceId ->
                    navController.navigate(Screen.Favorites.createRoute(workspaceId))
                },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.FolderBrowser.route,
            arguments = listOf(
                navArgument("workspaceId") { type = NavType.IntType },
                navArgument("workspaceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getInt("workspaceId") ?: return@composable
            val workspaceName = backStackEntry.arguments?.getString("workspaceName") ?: ""
            FolderBrowserScreen(
                workspaceId = workspaceId,
                workspaceName = java.net.URLDecoder.decode(workspaceName, "UTF-8"),
                onFolderClick = { folder ->
                    navController.navigate(
                        Screen.NoteList.createRoute(workspaceId, folder.id, folder.name)
                    )
                },
                onAllNotesClick = {
                    navController.navigate(
                        Screen.NoteList.createRoute(workspaceId, null, "All Notes")
                    )
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createNewRoute(workspaceId, null))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NoteList.route,
            arguments = listOf(
                navArgument("workspaceId") { type = NavType.IntType },
                navArgument("folderId") { type = NavType.IntType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getInt("workspaceId") ?: return@composable
            val folderIdRaw = backStackEntry.arguments?.getInt("folderId") ?: -1
            val folderId = if (folderIdRaw == -1) null else folderIdRaw
            val folderName = backStackEntry.arguments?.getString("folderName") ?: "Notes"
            NoteListScreen(
                workspaceId = workspaceId,
                folderId = folderId,
                folderName = java.net.URLDecoder.decode(folderName, "UTF-8"),
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteViewer.createRoute(noteId))
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createNewRoute(workspaceId, folderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NoteViewer.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            NoteViewerScreen(
                noteId = noteId,
                onEdit = { note ->
                    navController.navigate(
                        Screen.NoteEditor.createRoute(note.workspaceId, note.id, note.type)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "editor/{workspaceId}/{noteId}/{noteType}?folderId={folderId}",
            arguments = listOf(
                navArgument("workspaceId") { type = NavType.IntType },
                navArgument("noteId") { type = NavType.IntType },
                navArgument("noteType") { type = NavType.StringType },
                navArgument("folderId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getInt("workspaceId") ?: return@composable
            val noteIdRaw = backStackEntry.arguments?.getInt("noteId") ?: -1
            val noteId = if (noteIdRaw == -1) null else noteIdRaw
            val noteType = backStackEntry.arguments?.getString("noteType") ?: "markdown"
            val folderIdRaw = backStackEntry.arguments?.getInt("folderId") ?: -1
            val folderId = if (folderIdRaw == -1) null else folderIdRaw
            NoteEditorScreen(
                workspaceId = workspaceId,
                noteId = noteId,
                noteType = java.net.URLDecoder.decode(noteType, "UTF-8"),
                folderId = folderId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteViewer.createRoute(noteId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Favorites.route,
            arguments = listOf(navArgument("workspaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getInt("workspaceId") ?: return@composable
            FavoritesScreen(
                workspaceId = workspaceId,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteViewer.createRoute(noteId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Trash.route) {
            TrashScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
