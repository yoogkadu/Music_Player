package com.example.musicplayer.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes{
    @Serializable data object HomeScreen: Routes
    @Serializable data object BootNavigationGraph: Routes
    @Serializable data object PermissionScreen: Routes
}
