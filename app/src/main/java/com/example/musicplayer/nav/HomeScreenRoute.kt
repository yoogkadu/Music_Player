package com.example.musicplayer.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.musicplayer.R


sealed class HomeScreenRoute(
    @get:StringRes val stringId: Int,
    @get:DrawableRes val iconId: Int,
    val index: Int
    ) {
    object Home : HomeScreenRoute(R.string.home, R.drawable.rounded_home_24,0)
    object Search : HomeScreenRoute(R.string.search, R.drawable.round_search_24,1)

    object List : HomeScreenRoute(R.string.list, R.drawable.round_list_24,2)
    object Album : HomeScreenRoute(R.string.album, R.drawable.instrument_playlist_svgrepo_com,3)
    object Library : HomeScreenRoute(R.string.library, R.drawable.round_library_music_24,4)

    companion object {
        val routes = listOf(
            Home, Search, List,
            Album, Library

        )
        fun getByIndex(index: Int) = routes.first { it.index == index }

        fun getRouteByIndex(inputRoute : HomeScreenRoute) = routes.indexOf(inputRoute)
    }
}
