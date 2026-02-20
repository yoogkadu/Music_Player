package com.example.musicplayer.nav

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.musicplayer.R
import com.example.musicplayer.data.BootStrapState
import com.example.musicplayer.data.Song
import com.example.musicplayer.permission.AndroidPermissionMapper
import com.example.musicplayer.ui.bottomNavigation.BottomMiniMusicPlayer
import com.example.musicplayer.ui.bottomNavigation.BottomNavBar
import com.example.musicplayer.ui.screens.MainPlayer
import com.example.musicplayer.ui.screens.PermissionScreen
import com.example.musicplayer.ui.screens.SearchScreen
import com.example.musicplayer.ui.screens.SongListScreen
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.viewModels.BootStrapViewModel
import com.example.musicplayer.ui.viewModels.MusicViewModel
import kotlinx.coroutines.launch


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
            val musicUiState = musicViewModel.uiState.collectAsStateWithLifecycle()
            var isMainMusicPLayerVisible by rememberSaveable() { mutableStateOf(false) }
            val focusManager = LocalFocusManager.current
            val pageState = rememberPagerState(0) { HomeScreenRoute.routes.size }
            val scrollScope = rememberCoroutineScope()

            Scaffold (modifier = Modifier,
                bottomBar={
                    if(!isMainMusicPLayerVisible){
                        Column {
                            BottomMiniMusicPlayer(
                                Modifier,
                                song = musicUiState.value.currentSong,
                                onTogglePlay = {
                                    musicViewModel.togglePlayPause()
                                },
                                onClick = {
                                    isMainMusicPLayerVisible=true
                                },
                                isPlaying = musicUiState.value.isPlaying,
                                onSkipNext = { musicViewModel.skipToNext() },
                                onSkipPrevious = { musicViewModel.skipToPrevious() }
                            )
                            BottomNavBar(navigationList = HomeScreenRoute.routes,
                                onClick = {
                                    route->
                                    scrollScope.launch {
                                        pageState.animateScrollToPage(HomeScreenRoute.getRouteByIndex(route),
                                            animationSpec = spring()
                                        )
                                    }
                                },
                                currentItem = HomeScreenRoute.getByIndex(pageState.currentPage)
                                )
                        }
                    }

                },

            ){
                paddingValues ->
                Box(
                    modifier = modifier.padding(paddingValues)
                ) {
                   HorizontalPager(pageState) {
                       pageNo->
                           val currentRoute = HomeScreenRoute.getByIndex(pageNo)
                       HomePagerContent(
                           currentRoute,
                            musicUiState.value.songs,

                           musicUiState.value.isLoading,
                           onSongClick = { song->
                               musicViewModel.playSong(song)
                           },
                           onChangeText = { text->
                               musicViewModel.onSearchTextChange(text)
                           },
                           searchText = musicUiState.value.searchText,
                           searchSongList = musicUiState.value.searchedSongs

                       )
                   }
                    AnimatedVisibility(
                        visible = isMainMusicPLayerVisible,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        val totalDuration = musicViewModel.player.value?.duration ?: 1L
                        MainPlayer(
                            song = musicUiState.value.currentSong,
                            currentPosition = musicUiState.value.currentPosition,
                            onSeek = { musicViewModel.player.value?.seekTo(it) },
                            totalDuration = totalDuration,
                            onTogglePlay = { musicViewModel.togglePlayPause() },
                            onSkipNext = {
                                musicViewModel.skipToNext()
                            },
                            isPlaying = musicUiState.value.isPlaying,
                            onSkipPrevious = {
                                musicViewModel.skipToPrevious()
                            },
                            onBackAction = {
                                isMainMusicPLayerVisible=false
                            }
                        )
                    }



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


@Composable
fun TopAppBar(modifier: Modifier = Modifier,onClick : () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().clip(shape = RoundedCornerShape(19.dp))
    ) {
        IconButton(onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(painter = painterResource(R.drawable.rounded_arrow_back_24)
                ,contentDescription = null)
        }
        Spacer(modifier = Modifier.weight(8f))
    }
}

@Preview(showBackground = true)
@Composable
private fun TopAppBarDemo() {
    MusicPlayerTheme() {
        TopAppBar(modifier = Modifier, onClick = {})
    }
}

@Composable
fun HomePagerContent(
    route: HomeScreenRoute,
    songList: List<Song>,
    isLoading: Boolean,
    onSongClick: (Song) -> Unit,
    onChangeText: (String) -> Unit,
    searchText: String,
    searchSongList: List<Song>
    ) {
    when (route) {
        is HomeScreenRoute.Home -> Surface(modifier = Modifier.fillMaxSize()){ Text("Hello") }
        is HomeScreenRoute.Search -> SearchScreen(
            songList = searchSongList,
            modifier = Modifier,
            onSongClick = onSongClick,
            onChangeText = onChangeText,
            searchText =searchText,
        )
        is HomeScreenRoute.List -> {
            SongListScreen(
                songList =songList,
                isLoading = isLoading,
                onSongClick = onSongClick
            )
        }
        is HomeScreenRoute.Playlist -> Surface(modifier = Modifier.fillMaxSize()){ Text("Hello") }
    }
}