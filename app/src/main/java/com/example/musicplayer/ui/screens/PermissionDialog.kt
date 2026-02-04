package com.example.musicplayer.ui.screens

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayer.R
import com.example.musicplayer.permission.AppPermission
import com.example.musicplayer.permission.AudioDescriptionProvider
import com.example.musicplayer.permission.PermissionDescriptionProvider

@Composable
fun PermissionDialog (
    permissionTextProvider: PermissionDescriptionProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {}
            ) {
                Text("Grant Permission")
            }

        },
        title = {
            Text(text = stringResource(permissionTextProvider.getDescriptionRes(isPermanentlyDeclined=isPermanentlyDeclined)), fontSize =15.sp)
        },
        modifier = modifier,
        dismissButton = {
            Button(
                onClick = {
                    
                }
            ) {
                Text("Dismiss")
            }

        }

    )
}

@Preview
@Composable
private fun SeeDismissDialog() {
    PermissionDialog(
        AudioDescriptionProvider(),
        isPermanentlyDeclined = false,
        onDismiss = {  },
        onOkClick = { },
        onGoToAppSettingsClick = { }
    )
}