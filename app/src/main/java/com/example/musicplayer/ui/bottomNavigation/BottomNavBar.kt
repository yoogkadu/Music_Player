package com.example.musicplayer.ui.bottomNavigation

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicplayer.data.Song

@Composable
fun BottomNavBar(modifier: Modifier = Modifier,
                 song: Song?) {
    Row() {

    }

}

@Preview
@Composable
private fun BottomNavBarPreview() {
    BottomNavBar(song = null)
}