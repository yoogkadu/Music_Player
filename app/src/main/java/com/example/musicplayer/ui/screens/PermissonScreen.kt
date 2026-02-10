package com.example.musicplayer.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.example.musicplayer.permission.AppPermission
import com.example.musicplayer.permission.AudioDescriptionProvider
import com.example.musicplayer.permission.NotificationDescriptionProvider
import com.example.musicplayer.permission.PlatformPermissionMapper

@Composable
fun PermissionScreen(modifier: Modifier=Modifier,
                     dialogQueue: List<AppPermission> = emptyList(),
                     mapper: PlatformPermissionMapper,
                     onDismiss: () -> Unit,
                     onOkClick: (AppPermission) -> Unit,
                     onGrantClick: () ->Unit,
){

    val activity  = LocalActivity.current
    Scaffold {
        paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues).fillMaxSize(),
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
    }
    dialogQueue.
    reversed().
    forEach {permission ->
        val manifestPermission = mapper.map(permission)
     PermissionDialog(
         permissionTextProvider = when (permission) {
                 AppPermission.AudioLibrary-> AudioDescriptionProvider()
                 AppPermission.Notification -> NotificationDescriptionProvider()
         }
         ,
         isPermanentlyDeclined = activity?.let {
             act-> manifestPermission.any{
                 manifestString ->
             !shouldShowRequestPermissionRationale(act, manifestString)
            }
         } ?: false,
         onDismiss = {
             onDismiss()
                     },
         onOkClick = {
             onOkClick(permission)
         },
         onGoToAppSettingsClick = {
             activity?.openAppSettings()
             onDismiss()
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



//@Preview(showBackground =true)
//@Composable
//private fun PermissionScreenPreview() {
//    PermissionScreen(
//        onDismiss = {},
//        dialogQueue = listOf(AppPermission.AudioLibrary),
//        onOkClick = {},
//        onGrantClick = {},
//        mapper =
//
//    )
//}
//
