package com.example.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.musicplayer.R
import com.example.musicplayer.data.Song

@Composable
fun SongListScreen(modifier: Modifier = Modifier,
                   songList: List<Song>,
                   isLoading : Boolean) {
    if(isLoading){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
    else if (songList.isEmpty()){
        Box (modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            Text(stringResource(R.string.no_songs_found), fontSize = 16.sp)
        }
    }
    else{
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(songList) {
                SongCard(Modifier, it)
            }
        }
    }
}

@Composable
fun SongCard(modifier: Modifier = Modifier,song: Song) {

        ListItem(
            modifier=modifier.clickable(
                onClick = {
                    print(song.title)
                }
            ),
            headlineContent = { Text(song.title) },
            supportingContent = { Text(song.artist) },
            leadingContent = {
                // Place for Album Art
                AsyncImage(
                    model=song.albumArtUri,
                    contentDescription = song.album,
                    fallback = painterResource(R.drawable.baseline_music_note_24),
                    placeholder = painterResource(R.drawable.baseline_music_note_24),
                    error = painterResource(R.drawable.baseline_music_note_24),
                    modifier = Modifier
                        .size(50.dp) // Standard M3 list thumbnail size
                        .clip(RoundedCornerShape(8.dp)),
                )
            },
            trailingContent = {
                // Place for a "More" or "Favorite" button
                IconButton(onClick = { /* handle click */ }) {
                    Icon(painterResource(R.drawable.baseline_expand_more_24), contentDescription = stringResource(R.string.options))
                }
            },
            tonalElevation = 18.dp,
            shadowElevation = 100.dp
        )

}
@Preview(showBackground = true)
@Composable
private fun SongListScreenPreview() {
    val mockSongs = listOf(
        Song(id = "1", title = "Bohemian Rhapsody",
            artist = "Queen", duration = 3000L, 
            uri = null, albumArtUri = "".toUri(),album ="", albumArtist = "Unknown"),
        Song(id = "2", title = "Midnight City", artist = "M83",duration = 3000L, uri = null, albumArtUri = null, album = "", albumArtist = "Unknown"),
        Song(id = "3", title = "Starboy", artist = "The Weeknd",duration = 3000L, uri = null, albumArtUri = null, album = "", albumArtist = "Unknown")
    )
    SongListScreen(Modifier,emptyList(),isLoading = false)
}