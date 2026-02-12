package com.example.musicplayer.nav

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.musicplayer.AppViewModelProvider
import com.example.musicplayer.data.BootStrapState
import com.example.musicplayer.permission.AndroidPermissionMapper
import com.example.musicplayer.ui.bottomNavigation.BottomMiniMusicPlayer
import com.example.musicplayer.ui.bottomNavigation.BottomNavBar
import com.example.musicplayer.ui.screens.PermissionScreen
import com.example.musicplayer.ui.screens.SongListScreen
import com.example.musicplayer.ui.viewModels.BootStrapViewModel
import com.example.musicplayer.ui.viewModels.MusicViewModel


@Composable
fun MusicPlayerNavigation(modifier: Modifier= Modifier,
                          navController: NavHostController =rememberNavController()
) {

    NavHost(navController = navController, startDestination = Routes.BootNavigationGraph) {
        navigation<Routes.BootNavigationGraph>(startDestination = Routes.PermissionScreen){
            composable<Routes.PermissionScreen> {
                backstackEntry ->
                val bootStrapViewModel = backstackEntry.
                sharedViewModel<BootStrapViewModel, Routes.BootNavigationGraph>(
                    navController,
                    factory = AppViewModelProvider.Factory)

                val bootStrapUiState by bootStrapViewModel.uiState.collectAsStateWithLifecycle()
                val mapper = AndroidPermissionMapper()
                bootStrapViewModel.checkIfReadyToProceed()
                LaunchedEffect(bootStrapUiState) {

                    // If state is Loading (meaning we just got permission), jump immediately!
                    if (bootStrapUiState is BootStrapState.Loading) {
                        navController.navigate(Routes.HomeScreen) {
                            // This clears the Permission screen from the "Back" history
                            popUpTo<Routes.BootNavigationGraph> { inclusive = true }
                        }
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { permissions ->
                        permissions.forEach{
                            (permission, isGranted) -> bootStrapViewModel.onPermissionResult(permission ,isGranted)
                        }

                    }
                )
                if (bootStrapUiState is BootStrapState.NeedsPermission) {
                    PermissionScreen(
                        modifier = modifier,
                        dialogQueue = bootStrapViewModel.visiblePermissionDialogQueue,
                        mapper = mapper,
                        onOkClick = { permission ->
                            val stringsToRequest = mapper.map(permission).toTypedArray()
                            permissionLauncher.launch(stringsToRequest)
                        },
                        onDismiss = {
                            bootStrapViewModel.dismissDialog()
                        },
                        onGrantClick = {
                            val permission = bootStrapViewModel.getAllRequiredPermissions()
                            permissionLauncher.launch(permission)
                        },
                    )
                }
            }
        }
        composable< Routes.HomeScreen> {
            val musicViewModel : MusicViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val musicUiState = musicViewModel.songs.collectAsStateWithLifecycle()
            val musicLoadingState= musicViewModel.isLoading.collectAsStateWithLifecycle()

            Scaffold (
                bottomBar={
                    Column {
                        BottomMiniMusicPlayer(
                            Modifier,
                            song = musicViewModel.currentSong.collectAsState().value,
                            onTogglePlay = {
                                musicViewModel.togglePlayPause()
                            },
                            onClick = {
                            }, isPlaying = musicViewModel.isPlaying.collectAsState().value,
                            onSkipNext = { musicViewModel.skipToNext() },
                            onSkipPrevious = { musicViewModel.skipToPrevious() }
                        )
                        BottomNavBar(navigationList = getAllHomeScreenRoutes())
                    }
                }
            ){
                paddingValues ->
                Box(
                    modifier = modifier.padding(paddingValues)
                ) {
                    SongListScreen(
                        songList = musicUiState.value,
                        isLoading = musicLoadingState.value,
                        onSongClick = { song ->
                            musicViewModel.playSong(song)
                        }
                    )




                }

            }
        }

    }
}


@Composable
inline fun <reified T : ViewModel, reified R : Any>
        NavBackStackEntry.sharedViewModel(navController: NavHostController,
                                          factory: ViewModelProvider.Factory):T {
    val parentEntry = remember(this) {
        navController.getBackStackEntry<R>()
    }
    return viewModel(parentEntry, factory = factory)

}



