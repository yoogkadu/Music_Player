package com.example.musicplayer.ui.screens

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
import com.example.musicplayer.R
import com.example.musicplayer.data.Song
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun MainPlayer(modifier: Modifier = Modifier,
               song: Song?,
               currentPosition:Long,
               onSeek : (Long)->Unit,
               totalDuration : Long,
               isPlaying: Boolean,
               onTogglePlay : ()->Unit,
               onSkipNext : ()->Unit,
               onSkipPrevious : ()->Unit,
) {
    val context= LocalContext.current
    val placeholder =painterResource(R.drawable.baseline_music_note_24)

    // 2. Local state: This is what the Slider actually "looks" at
    var localSliderValue by remember { mutableFloatStateOf(0f) }

    // 3. The "Gate": Are we currently scrubbing?
    var isUserScrubbing by remember { mutableStateOf(false) }
    LaunchedEffect(currentPosition) {
        if (!isUserScrubbing) {
            localSliderValue = currentPosition.toFloat()
        }
    }
    Surface(modifier.fillMaxSize()) {

        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song?.albumArtUri)
                    .placeholder(R.drawable.baseline_music_note_24)
                    .build(),
                contentDescription = song?.artist,
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
                modifier = modifier
                    .padding(20.dp)
                    .size(400.dp)
                    .clickable(onClick = onTogglePlay),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp))
            Text(
                song?.title ?: stringResource(R.string.unknown_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1
            )
            Text(
                song?.artist ?: stringResource(R.string.unknown_artist),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

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
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    inactiveTickColor = Color.Transparent
                )

            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPosition.coerceIn(0, totalDuration.coerceAtLeast(1L))
                ))
                Text(formatTime(totalDuration.coerceAtLeast(1L)

                ))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        painterResource(R.drawable.round_skip_previous_24),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
                FloatingActionButton(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        if (isPlaying) painterResource(R.drawable.rounded_pause_24) else painterResource(
                            R.drawable.rounded_play_arrow_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = onSkipNext) {
                    Icon(
                        painterResource(R.drawable.round_skip_next_24),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

}

@Preview
@Composable
private fun MainPlayerPreview() {
    MusicPlayerTheme() {
        MainPlayer(Modifier, song, 0, {}, 0, false, {}, {},{})
    }
}

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

//@Composable
//fun UsuableSlider(modifier: Modifier = Modifier,
//                  currentPosition: Float,
//                  onSeek : (Float)->Unit,
//                  totalDuration : Float,
//                  isPlaying: Boolean,
//                  userScrubbing: ()->Unit,
//                  userFinishedScrubbing : (Float)->Unit,
//                  color: SliderColors = SliderDefaults.colors()
//                  ) {
//    val safeCurrentPosition = currentPosition.coerceIn(0f, totalDuration.coerceAtLeast(1f))
//    val safeTotalDuration = totalDuration.coerceAtLeast(1f)
//    Slider(
//        value = safeCurrentPosition,
//        o
//        onValueChangeFinished = {},
//
//
//    )
//
//}