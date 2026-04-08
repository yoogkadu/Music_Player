package com.example.musicplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.musicplayer.R
import com.example.musicplayer.data.Song
import com.example.musicplayer.nav.TopAppBar
import com.example.musicplayer.ui.theme.MusicPlayerTheme


@Composable
fun MainPlayer(modifier: Modifier = Modifier,
               song: Song?,
               currentPosition:Long,
               onSeek : (Long)->Unit,
               totalDuration : Long,
               isPlaying: Boolean,
               isShuffled: Boolean = false,
               isRepeatEnabled: Boolean = false,
               onTogglePlay : ()->Unit,
               onSkipNext : ()->Unit,
               onSkipPrevious : ()->Unit,
               onToggleShuffle: () -> Unit,
               onToggleRepeat: () -> Unit = {},
               onBackAction: ()->Unit
) {
    val context= LocalContext.current
    val placeholder =painterResource(R.drawable.baseline_music_note_24)

    var localSliderValue by remember { mutableFloatStateOf(0f) }
    var isUserScrubbing by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition) {
        if (!isUserScrubbing) {
            localSliderValue = currentPosition.toFloat()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onBackAction
            )

            BackHandler(enabled = true) {
                onBackAction()
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Album Art
            Surface(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song?.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    placeholder = placeholder,
                    error = placeholder,
                    fallback = placeholder,
                    modifier = Modifier.fillMaxSize().clickable(onClick = onTogglePlay),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Title and Artist
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song?.title ?: stringResource(R.string.unknown_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = song?.artist ?: stringResource(R.string.unknown_artist),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = localSliderValue.coerceIn(0f, totalDuration.toFloat().coerceAtLeast(0f)),
                    onValueChange = {
                        isUserScrubbing = true
                        localSliderValue = it
                    },
                    onValueChangeFinished = {
                        isUserScrubbing = false
                        onSeek(localSliderValue.toLong())
                    },
                    valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition.coerceIn(0, totalDuration.coerceAtLeast(1L))),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(totalDuration.coerceAtLeast(1L)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Playback Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        painter = painterResource(R.drawable.round_shuffle_24),
                        contentDescription = "Shuffle",
                        tint = if (isShuffled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        painter = painterResource(R.drawable.round_skip_previous_24),
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.rounded_pause_24) 
                                 else painterResource(R.drawable.rounded_play_arrow_24),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(
                        painter = painterResource(R.drawable.round_skip_next_24),
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        painter = painterResource(R.drawable.round_repeat_24),
                        contentDescription = "Repeat",
                        tint = if (isRepeatEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

//@Preview
//@Composable
//private fun MainPlayerPreview() {
//    MusicPlayerTheme() {
//        MainPlayer(Modifier, song, 0, {}, 0, false, {}, {},{},{})
//    }
//}

val song = Song(
    "123",
    title = "Artist",
    duration = 123L,
    uri = null,
    artist = "Artist",
    albumArtist = "Album Artist",
    albumArtUri = "https://mars.nasa.gov/system/downloadable_items/39175_global-color-views-mars-PIA00407.jpg".toUri(),
    album = "Album"
)
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}