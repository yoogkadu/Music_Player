package com.example.musicplayer.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.R
import com.example.musicplayer.data.Song


@Composable
fun SearchScreen(modifier: Modifier = Modifier,
                 songList: List<Song>,
                 onSongClick : (Song) -> Unit,
                 onChangeText: (String) -> Unit,
                 searchText: String) {
    val context = LocalContext.current
    val placeHolderImg = painterResource(R.drawable.baseline_music_note_24)
    Column() {
        SearchBar(
            modifier = Modifier.statusBarsPadding(),
            searchText = searchText,
            onChangeText = onChangeText
        )
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(
                    songList,
                    key = { it.id }) {
                    SearchSongCard(
                        Modifier,
                        it,
                        onSongClick = onSongClick,
                        context,
                        placeHolderImg,
                    )
                }

            }

    }
}
@Composable
fun SearchBar(modifier: Modifier = Modifier,
              searchText:String = "",
              onChangeText : (String) -> Unit) {
        val iconImg = if(searchText.isEmpty()) painterResource(R.drawable.round_search_24)
                else painterResource(R.drawable.rounded_arrow_back_24)
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val onIconButtonClick = {
            if(searchText.isEmpty()) {
                keyboardController?.show()
            }
            else{
                keyboardController?.hide()
                focusManager.clearFocus()
                onChangeText("")
            }
        }
        TextField(value = searchText,
            modifier = modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            onValueChange = {
                onChangeText(it)
                Log.d("Music Debug",it)
            },
            placeholder = {
                          Text(stringResource(R.string.search_placeholder))
                        },
            leadingIcon = {
                IconButton(onClick = {
                    if(searchText.isNotEmpty()) {
                        KeyboardActions { }
                    }
                }) {
                    Icon(iconImg,
                        contentDescription = if (searchText.isEmpty()) stringResource(R.string.search_placeholder)
                        else stringResource(R.string.back)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),

        )
}

@Composable
fun SearchSongCard(modifier: Modifier = Modifier,
                   song: Song,
                   onSongClick: (Song) -> Unit = {}
                   ,context: Context,
                   placeHolderImage : Painter) {
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
        tonalElevation = 4.dp,
        shadowElevation = 10.dp
    )

    
}


@Preview(showBackground = true)
@Composable
private fun DisplaySearchScreen() {
    SearchScreen(
        songList = emptyList(),
        modifier = Modifier,
        onSongClick = {},
        searchText = "",
        onChangeText = {}
    )
}