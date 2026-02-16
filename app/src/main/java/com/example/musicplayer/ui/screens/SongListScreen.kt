package com.example.musicplayer.ui.screens

import android.content.Context
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song

@Composable
fun SongListScreen(modifier: Modifier = Modifier,
                   songList: List<Song>,
                   isLoading : Boolean,
                   onSongClick: (Song) -> Unit,
                   ) {
    val context = LocalContext.current
    val placeholderImg = painterResource(R.drawable.baseline_music_note_24)
    val expandMoreImg = painterResource(R.drawable.baseline_expand_more_24)
    if(isLoading){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,

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
            items(songList,
                key = {it.id}) {
                SongCard(Modifier,
                    it,
                    onSongClick=onSongClick,
                    context,
                    placeholderImg,
                    expandMoreImg)
            }
        }
    }
}

@Composable
fun SongCard(modifier: Modifier = Modifier,
             song: Song,
             onSongClick: (Song) -> Unit = {}
            ,context: Context,
             placeHolderImage : Painter,
             expandMoreImg: Painter
) {

        ListItem(
            modifier=modifier.clickable(
                onClick = {
                    onSongClick(song)
                }
            ),
            headlineContent = { Text(song.title) },
            supportingContent = { Text(song.artist) },
            leadingContent = {
                AsyncImage(
                    model= remember(song.albumArtUri) {
                        ImageRequest.Builder(context)
                            .data(song.albumArtUri)
                            .size(150, 150)
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = song.album,
                    placeholder = placeHolderImage,
                    error = placeHolderImage,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    fallback = placeHolderImage
                )

            },
            trailingContent = {
                // Place for a "More" or "Favorite" button
                IconButton(onClick = { /* handle click */ }) {
                    Icon(expandMoreImg,
                        contentDescription = stringResource(R.string.options))
                }
            },
            tonalElevation = 4.dp,
            shadowElevation = 10.dp
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
    SongListScreen(Modifier,mockSongs,isLoading = false, onSongClick = {

    })
}