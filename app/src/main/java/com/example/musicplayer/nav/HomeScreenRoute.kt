package com.example.musicplayer.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.musicplayer.R


sealed class HomeScreenRoute(
    @get:StringRes val stringId: Int,
    @get:DrawableRes val iconId: Int,
    val isSelected: Boolean
    ) {
    object Home : HomeScreenRoute(R.string.home, R.drawable.rounded_home_24,false)
    object Search : HomeScreenRoute(R.string.search, R.drawable.round_search_24,false)
    object List : HomeScreenRoute(R.string.list, R.drawable.round_list_24,true)
    object Playlist : HomeScreenRoute(R.string.playlist, R.drawable.instrument_playlist_svgrepo_com,false)
}

fun getAllHomeScreenRoutes() : List<HomeScreenRoute>{
    return listOf(HomeScreenRoute.Home, HomeScreenRoute.Search, HomeScreenRoute.List,
        HomeScreenRoute.Playlist)
}