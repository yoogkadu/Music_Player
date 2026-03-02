package com.example.musicplayer.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song

@Composable
fun AlbumScreen(modifier: Modifier = Modifier,
                albumList: Map<String,List<Song>>,
                selectedAlbum: String= "",
                onAlbumClick: (String) -> Unit,
                isAlbumSelected: Boolean,
                toggleAlbumSelected: ()->Unit,
                onSongClick: (Song)->Unit,
) {

    if(!isAlbumSelected){
        AlbumListDisplay(modifier = modifier, playlists = albumList,
            onAlbumClick = { albumTitle ->
                toggleAlbumSelected()
                onAlbumClick(albumTitle)
            }
        )
    }
    else{
        AlbumContentListScreen(modifier = modifier, album = albumList[selectedAlbum] ?: emptyList(), onBack = toggleAlbumSelected, onSongClick =onSongClick )
    }

}

@Composable
fun AlbumListDisplay(modifier: Modifier = Modifier,
                     playlists: Map<String,List<Song>>,
                     onAlbumClick : (String)-> Unit,
                     )
{

    val defaultIcon = painterResource(R.drawable.baseline_music_note_24)
    val context = LocalContext.current
    val playlistEntries = playlists.entries.toList()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(130.dp),
        modifier = modifier.fillMaxWidth(),
        userScrollEnabled = true,
        contentPadding = PaddingValues(16.dp),
    ) {
        items(items = playlistEntries, key = {entry ->entry.key}){
                entry ->
            val playlistName = entry.key
            val songsList = entry.value

            if (songsList.isNotEmpty()) {
                val firstSong = songsList.first()

                PlayListCard(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(4.dp)
                        .clickable(onClick = {
                            onAlbumClick(playlistName)
                        }),
                    albumArtUri = firstSong.albumArtUri,
                    albumTitle = playlistName,
                    artist = firstSong.artist,
                    context = context,
                    errorIcon = defaultIcon
                )
            }
        }
    }
}
@Composable
fun PlayListCard(modifier: Modifier = Modifier, albumArtUri: Uri?,
                 errorIcon: Painter,albumTitle: String,artist: String,
                 context: Context, ) {
    Card(modifier) {
         Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(

                model = ImageRequest.Builder(context)
                    .data(albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = albumTitle,
                placeholder = errorIcon,
                error = errorIcon,
                fallback = errorIcon, modifier = Modifier.size(100.dp),
                onError = {error->
                    Log.e("MusicDebug",error.toString())

                }
            )

            Text(
                text = albumTitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun PlayListScreenPreview() {
//    PlayListScreen(playlists = dummyPlaylist)
//}

//@Preview
//@Composable
//private fun PlaylistCardDemo() {
//    PlayListCard(
//        modifier = Modifier,
//        song.albumArtUri,
//        errorIcon = painterResource(R.drawable.baseline_music_note_24),
//        artist = song.artist,
//        albumTitle = song.album,
//        context = LocalContext.current
//    )
//}
