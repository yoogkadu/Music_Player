package com.example.musicplayer.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song

@Composable
fun AlbumScreen(
        modifier: Modifier = Modifier,
        albumList: Map<String, List<Song>>,
        selectedAlbum: String = "",
        onAlbumClick: (String) -> Unit,
        isAlbumSelected: Boolean,
        toggleAlbumSelected: () -> Unit,
        onSongClick: (Song) -> Unit,
) {
    if (!isAlbumSelected) {
        AlbumListDisplay(
                modifier = modifier,
                playlists = albumList,
                onAlbumClick = { albumTitle ->
                    toggleAlbumSelected()
                    onAlbumClick(albumTitle)
                }
        )
    } else {
        AlbumContentListScreen(
                modifier = modifier,
                album = albumList[selectedAlbum] ?: emptyList(),
                onBack = toggleAlbumSelected,
                onSongClick = onSongClick
        )
    }
}

@Composable
fun AlbumListDisplay(
        modifier: Modifier = Modifier,
        playlists: Map<String, List<Song>>,
        onAlbumClick: (String) -> Unit,
) {
    if (playlists.isEmpty()) {
        AlbumListEmptyState(modifier = modifier.fillMaxSize())
    } else {
        val defaultIcon = painterResource(R.drawable.baseline_music_note_24)
        val context = LocalContext.current
        val playlistEntries = playlists.entries.toList()

        LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = modifier.fillMaxWidth(),
                userScrollEnabled = true,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = playlistEntries, key = { entry: Map.Entry<String, List<Song>> -> entry.key }) { entry: Map.Entry<String, List<Song>> ->
                val playlistName = entry.key
                val songsList = entry.value

                if (songsList.isNotEmpty()) {
                    val firstSong = songsList.first()

                    PlayListCard(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .clickable(onClick = { onAlbumClick(playlistName) }),
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
}

@Composable
fun AlbumListEmptyState(modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                painter = painterResource(R.drawable.baseline_music_note_24),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
                text = "No Albums Found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "Your music library is currently empty.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlayListCard(
        modifier: Modifier = Modifier,
        albumArtUri: Uri?,
        errorIcon: Painter,
        albumTitle: String,
        artist: String,
        context: Context,
) {
    Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            AsyncImage(
                    model = ImageRequest.Builder(context).data(albumArtUri).crossfade(true).build(),
                    contentDescription = albumTitle,
                    placeholder = errorIcon,
                    error = errorIcon,
                    fallback = errorIcon,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)),
                    onError = { error -> Log.e("MusicDebug", error.toString()) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = albumTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )

            Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// private fun PlayListScreenPreview() {
//    PlayListScreen(playlists = dummyPlaylist)
// }

// @Preview
// @Composable
// private fun PlaylistCardDemo() {
//    PlayListCard(
//        modifier = Modifier,
//        song.albumArtUri,
//        errorIcon = painterResource(R.drawable.baseline_music_note_24),
//        artist = song.artist,
//        albumTitle = song.album,
//        context = LocalContext.current
//    )
// }
