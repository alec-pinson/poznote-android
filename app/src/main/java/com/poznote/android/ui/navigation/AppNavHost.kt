package com.poznote.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.net.URLDecoder

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
                onWorkspaceClick = { workspaceName ->
                    navController.navigate(Screen.FolderBrowser.createRoute(workspaceName))
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onFavoritesClick = { workspaceName ->
                    navController.navigate(Screen.Favorites.createRoute(workspaceName))
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
                navArgument("workspaceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workspaceName = URLDecoder.decode(
                backStackEntry.arguments?.getString("workspaceName") ?: "", "UTF-8"
            )
            FolderBrowserScreen(
                workspaceName = workspaceName,
                onFolderClick = { folder ->
                    navController.navigate(
                        Screen.NoteList.createRoute(workspaceName, folder.id, folder.name)
                    )
                },
                onAllNotesClick = {
                    navController.navigate(
                        Screen.NoteList.createRoute(workspaceName, null, "All Notes")
                    )
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createNewRoute(workspaceName, null))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NoteList.route,
            arguments = listOf(
                navArgument("workspaceName") { type = NavType.StringType },
                navArgument("folderId") { type = NavType.IntType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workspaceName = URLDecoder.decode(
                backStackEntry.arguments?.getString("workspaceName") ?: "", "UTF-8"
            )
            val folderIdRaw = backStackEntry.arguments?.getInt("folderId") ?: -1
            val folderId = if (folderIdRaw == -1) null else folderIdRaw
            val folderName = URLDecoder.decode(
                backStackEntry.arguments?.getString("folderName") ?: "Notes", "UTF-8"
            )
            NoteListScreen(
                workspaceName = workspaceName,
                folderId = folderId,
                folderName = folderName,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteViewer.createRoute(noteId))
                },
                onCreateNote = {
                    navController.navigate(Screen.NoteEditor.createNewRoute(workspaceName, folderId))
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
                        Screen.NoteEditor.createRoute(note.workspace, note.id, note.type)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "editor/{workspaceName}/{noteId}/{noteType}?folderId={folderId}",
            arguments = listOf(
                navArgument("workspaceName") { type = NavType.StringType },
                navArgument("noteId") { type = NavType.IntType },
                navArgument("noteType") { type = NavType.StringType },
                navArgument("folderId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val workspaceName = URLDecoder.decode(
                backStackEntry.arguments?.getString("workspaceName") ?: "", "UTF-8"
            )
            val noteIdRaw = backStackEntry.arguments?.getInt("noteId") ?: -1
            val noteId = if (noteIdRaw == -1) null else noteIdRaw
            val noteType = URLDecoder.decode(
                backStackEntry.arguments?.getString("noteType") ?: "markdown", "UTF-8"
            )
            val folderIdRaw = backStackEntry.arguments?.getInt("folderId") ?: -1
            val folderId = if (folderIdRaw == -1) null else folderIdRaw
            NoteEditorScreen(
                workspaceName = workspaceName,
                noteId = noteId,
                noteType = noteType,
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
            arguments = listOf(navArgument("workspaceName") { type = NavType.StringType })
        ) { backStackEntry ->
            val workspaceName = URLDecoder.decode(
                backStackEntry.arguments?.getString("workspaceName") ?: "", "UTF-8"
            )
            FavoritesScreen(
                workspaceName = workspaceName,
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
