package com.example.musicplayer.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.AppViewModelProvider
import com.example.musicplayer.ui.screens.SongListScreen
import com.example.musicplayer.ui.viewModels.MusicViewModel


@Composable
fun MusicPlayerNavigation(modifier: Modifier= Modifier,navController: NavHostController =rememberNavController()) {
    val musicViewModel : MusicViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val musicUiState = musicViewModel.songs.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.SongList) {
        composable< Routes.SongList> {
            SongListScreen(
                modifier = modifier,
                songList = musicUiState.value
            )
        }
    }
}


