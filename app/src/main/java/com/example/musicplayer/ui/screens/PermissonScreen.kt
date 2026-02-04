package com.example.musicplayer.ui.screens

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicplayer.AppViewModelProvider
import com.example.musicplayer.MainActivity
import com.example.musicplayer.permission.AppPermission
import com.example.musicplayer.permission.AudioDescriptionProvider
import com.example.musicplayer.permission.NotificationDescriptionProvider
import com.example.musicplayer.ui.viewModels.BootStrapViewModel

@Composable
fun PermissionScreen(modifier: Modifier=Modifier,
                     onDismiss: () -> Unit,
                     dialogQueue: List<AppPermission> = mutableListOf(),
                     onOkClick: (AppPermission) -> Unit,
                     onGrantClick: () ->Unit
){
    val activity  = LocalActivity.current
    Column(
        modifier=modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Music access is required to continue.",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = onGrantClick,
        ) {
            Text(text = "Request Audio Permission")

        }

    }
    dialogQueue.forEach {permission ->
     PermissionDialog(
         permissionTextProvider = when (permission) {
                 AppPermission.READ_AUDIO , AppPermission.READ_EXTERNAL_STORAGE-> AudioDescriptionProvider()
                 AppPermission.POST_NOTIFICATIONS -> NotificationDescriptionProvider()
         }
         ,
         isPermanentlyDeclined = activity?.let {
             !shouldShowRequestPermissionRationale(it, permission.name)
         } ?: false,
         onDismiss = onDismiss,
         onOkClick = {
             onOkClick(permission)
         },
         onGoToAppSettingsClick = {
             activity?.openAppSettings()
                                  },
     )
    }

}

fun Activity.openAppSettings(){
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}



@Preview(showBackground =true)
@Composable
private fun PermissionScreenPreview() {
    PermissionScreen(
        onDismiss = {},
        dialogQueue = listOf(AppPermission.READ_AUDIO),
        onOkClick = {},
        onGrantClick = {}

    )
}

