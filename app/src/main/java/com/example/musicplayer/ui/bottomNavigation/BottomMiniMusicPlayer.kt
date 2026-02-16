package com.example.musicplayer.ui.bottomNavigation

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@Composable
fun BottomMiniMusicPlayer(modifier: Modifier = Modifier,
                          song: Song?,
                          onClick: () -> Unit,
                          onTogglePlay : () -> Unit,
                          isPlaying: Boolean = false,
                          onSkipNext: ()->Unit,
                          onSkipPrevious: ()->Unit,
                          ) {
    val context = LocalContext.current
    Surface (modifier = modifier
        .clip(shape = RoundedCornerShape(topStart = CornerSize(10.dp), topEnd = CornerSize(10.dp)
        , bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)))
        .fillMaxWidth()
        .height(68.dp)
        .clickable(
            onClick = onClick
        ),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,

    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)

        ) {
            AsyncImage(
                model= ImageRequest.Builder(context)
                    .data(song?.albumArtUri)
                    .placeholder(R.drawable.baseline_music_note_24)
                    .size(120,120)
                    .crossfade(true)
                    .build(),
                contentDescription = song?.album,
                error = painterResource(R.drawable.baseline_music_note_24),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)) {
                Text(
                    text = song?.title ?: stringResource(R.string.unknown_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song?.artist ?: stringResource(R.string.unknown_artist),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painterResource(
                        R.drawable.round_skip_previous_24
                    ),
                    contentDescription = stringResource(R.string.next)
                )

            }
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painterResource(
                        if (isPlaying) R.drawable.rounded_pause_24
                        else R.drawable.rounded_play_arrow_24
                    ),
                    contentDescription = stringResource(R.string.pause_play)
                )

            }
            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painterResource(
                        R.drawable.round_skip_next_24
                    ),
                    contentDescription = null
                )

            }

        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    name="Darkmode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BottomMiniPlayerPreview() {
    MusicPlayerTheme() {
        BottomMiniMusicPlayer(song = null, onClick = {}, onTogglePlay = {}, onSkipNext = {}, onSkipPrevious = {})
    }

}