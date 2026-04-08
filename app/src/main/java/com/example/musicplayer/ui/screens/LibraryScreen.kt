package com.example.musicplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.table.PlaylistEntity

@Composable
fun LibraryScreen(
    onLibraryItemClick: (LibraryItems) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    playlists: Map<PlaylistEntity,List<Song> > = emptyMap(),
    songList : List<Song>,
    onCreatePlaylist : (playlistName : String, songList : List<Song> ) -> Unit
) {
    Box{
        var isAddScreenOpen by remember { mutableStateOf(false) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LibraryItems.items.forEach { item ->
                        LibraryPillChip(
                            label = stringResource(item.stringId),
                            onClick = { onLibraryItemClick(item) }
                        )
                    }
                }
            }

            // 3. Playlists Section Header
            item {
                Text(
                    text = "Your Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (playlists.isEmpty()) {
                item { EmptyState(onAddClick = {
                    isAddScreenOpen = true
                }) }
            } else {
                items(playlists.entries.toList()) { playlistEntry ->
                    PlaylistCard(
                        playlist = playlistEntry.key,
                        onClick = { onPlaylistClick(playlistEntry.key) },
                        uri = playlistEntry.value.firstOrNull()?.albumArtUri ?: Uri.EMPTY
                    )
                }
            }
        }
        if(isAddScreenOpen){
            BackHandler() {
                isAddScreenOpen=false
            }
            CreatePlaylistOverlay(songList = songList, onDismissRequest = { isAddScreenOpen=false },
                onCreatePlaylist = {
                    playlistName, selectedSongs ->
                    onCreatePlaylist(playlistName,selectedSongs)
                    isAddScreenOpen=false
                }
            )
        }
    }

}

@Composable
fun EmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Surface(
                onClick = onAddClick,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_add_24),
                        contentDescription = "Create Playlist",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                }
            }
            Spacer(modifier.height(20.dp))
            Text(stringResource(R.string.addYourFirstPlaylist))
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun LibraryScreenPrev() {
//    MusicPlayerTheme() {
//        LibraryScreen(
//                onLibraryItemClick = {},
//                onPlaylistClick = {},
//                onAddPlaylistClick = {},
//                           playlists = listOf(
//                               Pair(
//                                   PlaylistEntity(name = "Chill Vibes", createdAt =
//                 System.currentTimeMillis()),
//                                    Uri.EMPTY
//                                ),
//                               Pair(
//                                    PlaylistEntity(name = "Gym Motivation", createdAt =
//                 System.currentTimeMillis()),
//                                   Uri.EMPTY
//                               ),
//                               Pair(
//                                  PlaylistEntity(name = "Coding Flow", createdAt =
//                 System.currentTimeMillis()),
//                                    Uri.EMPTY
//                                )
//                           )
//                )
//    }
//}

sealed class LibraryItems(@param:StringRes val stringId: Int) {
    object MostPlayed : LibraryItems(R.string.most_played)
    object Favorites : LibraryItems(R.string.favorites)
    object RecentlyPlayed : LibraryItems(R.string.recently_played)
    companion object {
        val items = listOf(MostPlayed, Favorites, RecentlyPlayed)
    }
}

@Composable
fun LibraryPillChip(label: String, onClick: () -> Unit) {
    Surface(
            onClick = onClick,
            shape = CircleShape, // The Pill Shape
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun PlaylistCard(playlist: PlaylistEntity, uri: Uri, onClick: () -> Unit) {
    Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                        contentDescription = "Playlist Cover",
                        contentScale = ContentScale.Crop,
                        placeholder =
                                painterResource(id = R.drawable.instrument_playlist_svgrepo_com),
                        error = painterResource(id = R.drawable.baseline_music_note_24),
                        fallback = painterResource(id = R.drawable.baseline_music_note_24)
                )
            }

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                Text(
                        "Playlist • Created ${formatTime(playlist.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
