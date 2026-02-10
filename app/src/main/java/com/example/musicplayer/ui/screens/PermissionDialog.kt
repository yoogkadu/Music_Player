package com.example.musicplayer.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permission Required", fontWeight = FontWeight.Bold, fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(text = stringResource(
                permissionTextProvider.getDescriptionRes(
                    isPermanentlyDeclined=isPermanentlyDeclined)),
                fontSize =15.sp)
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isPermanentlyDeclined) {
                        onGoToAppSettingsClick()
                    } else {
                        onOkClick()
                    }
                }
            ) {
                Text(if (isPermanentlyDeclined) "Open Settings" else "Grant Permission")
            }

        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
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
        permissionTextProvider = AudioDescriptionProvider(),
        isPermanentlyDeclined = false,
        onDismiss = { /* Preview: nothing happens */ },
        onOkClick = { },
        onGoToAppSettingsClick = { }
    )
}