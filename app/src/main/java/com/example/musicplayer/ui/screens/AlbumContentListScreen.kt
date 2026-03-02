package com.example.musicplayer.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song

@Composable
fun AlbumContentListScreen(modifier: Modifier = Modifier,album: List<Song>,
                           onBack: ()-> Unit,
                           onSongClick: (Song) -> Unit) {
    BackHandler() {
        onBack()
    }
    if(album.isNotEmpty()){
            val firstSong = album.first()
            val lazyColumnState = rememberLazyListState()
            val context = LocalContext.current
            val defaultIcon = painterResource(R.drawable.baseline_music_note_24)
            val expandMore = painterResource(R.drawable.baseline_expand_more_24)
            LazyColumn(
                modifier=modifier,
                state = lazyColumnState,
                userScrollEnabled = true
            ) {
                stickyHeader(){
                        StickyAlbumHeader(song = firstSong, painter = defaultIcon, context = context)
                }
               items(album,key={
                   it.title
               }){
                   song->
                   SongCard(
                       Modifier,
                       song=song,
                       onSongClick = onSongClick,
                       context,
                       defaultIcon,
                       expandMore
                   )

               }
            }
        }
    else{   //Handle the case when the album is empty
        Column(modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.albumEmptyText),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun StickyAlbumHeader(modifier: Modifier = Modifier,song: Song,painter: Painter,context: Context) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row{
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .size(200)
                    .build(),
                placeholder = painter,
                error = painter,
                contentDescription = song.album,

            )
            Column() {
                Text(
                    text = song.album
                )
                Spacer(modifier.height(20.dp))
                Text(
                    text = song.artist
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StickyHeaderPreview() {
    StickyAlbumHeader(song = song,
        painter = painterResource(R.drawable.baseline_music_note_24),
        context = LocalContext.current)

}