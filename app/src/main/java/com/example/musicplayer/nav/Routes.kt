package com.example.musicplayer.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes{
    @Serializable data object NowPlaying: Routes

    @Serializable data object SongList: Routes
}
