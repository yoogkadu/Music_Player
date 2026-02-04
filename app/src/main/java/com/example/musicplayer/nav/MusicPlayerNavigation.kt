package com.example.musicplayer.nav

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
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
import com.example.musicplayer.permission.toAppPermission
import com.example.musicplayer.ui.screens.PermissionScreen
import com.example.musicplayer.ui.screens.SongListScreen
import com.example.musicplayer.ui.viewModels.BootStrapViewModel
import com.example.musicplayer.ui.viewModels.MusicViewModel


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MusicPlayerNavigation(modifier: Modifier= Modifier,navController: NavHostController =rememberNavController()) {
    val musicViewModel : MusicViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val musicUiState = musicViewModel.songs.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.BootNavigationGraph) {
        navigation<Routes.BootNavigationGraph>(startDestination = Routes.PermissionScreen){

            composable<Routes.PermissionScreen> {
                backstackEntry ->
                val bootStrapViewModel = backstackEntry.
                sharedViewModel<BootStrapViewModel, Routes.BootNavigationGraph>(navController,
                    factory = AppViewModelProvider.Factory)

                val bootStrapUiState by bootStrapViewModel.uiState.collectAsStateWithLifecycle()
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { permissions ->
                        permissions.forEach {
                            (permission, isGranted) ->
                                permission.toAppPermission()?.let { appPer ->
                                        bootStrapViewModel.onPermissionResult(
                                            appPer, isGranted
                                        )
                            }


                        }
                    }
                )

                PermissionScreen(
                    modifier = modifier,

                    dialogQueue = bootStrapViewModel.visiblePermissionDialogQueue,
                    onOkClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    onDismiss = {
                        bootStrapViewModel.dismissDialog()
                    },
                    onGrantClick = {

                    }
                )
            }
            composable<Routes.LoadingScreen>{

            }
        }
        composable< Routes.SongList> {
            SongListScreen(
                modifier = modifier,
                songList = musicUiState.value
            )
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



