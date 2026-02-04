package com.example.musicplayer.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes{
    @Serializable data object NowPlaying: Routes

    @Serializable data object SongList: Routes
    @Serializable data object LoadingScreen: Routes
    @Serializable data object ErrorScreen: Routes
    @Serializable data object BootNavigationGraph: Routes
    @Serializable data object PermissionScreen: Routes
    @Serializable data object BootScreen: Routes
}

