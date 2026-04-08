package com.example.musicplayer.ui.screens

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.musicplayer.database.table.PlaylistEntity

@Composable
fun PlaylistContentListScreen(
    modifier: Modifier = Modifier,
    playlist: PlaylistEntity,
    songs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    BackHandler {
        onBack()
    }
    
    if (songs.isNotEmpty()) {
        val lazyColumnState = rememberLazyListState()
        val context = LocalContext.current
        val defaultIcon = painterResource(R.drawable.instrument_playlist_svgrepo_com)
        val expandMore = painterResource(R.drawable.baseline_expand_more_24)
        
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = lazyColumnState,
            userScrollEnabled = true
        ) {
           item {
               StickyPlaylistHeader(
                   playlist = playlist,
                   songs = songs,
                   painter = defaultIcon,
                   context = context
               )
           }
            items(songs, key = { it.id }) { song ->
                SongCard(
                    modifier = Modifier.fillMaxWidth(),
                    song = song,
                    onSongClick = onSongClick,
                    context = context,
                    placeHolderImage = painterResource(R.drawable.baseline_music_note_24),
                    expandMoreImg = expandMore
                )
            }
        }
    } else {
        PlaylistTracksEmptyState(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun PlaylistTracksEmptyState(modifier: Modifier = Modifier) {
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
            text = "Playlist is Empty",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No tracks found in this playlist.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StickyPlaylistHeader(
    modifier: Modifier = Modifier,
    playlist: PlaylistEntity,
    songs: List<Song>,
    painter: Painter,
    context: Context
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val firstSongArt = songs.firstOrNull()?.albumArtUri
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(firstSongArt)
                    .crossfade(true)
                    .build(),
                placeholder = painter,
                error = painter,
                fallback = painter,
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Playlist",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
