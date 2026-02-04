package com.example.musicplayer.permission

import android.Manifest
import android.os.Build
import com.example.musicplayer.R

sealed class AppPermission(val isOptional : Boolean) {
    data object AudioLibrary : AppPermission(isOptional = false)
    data object Notification : AppPermission(isOptional = true)

}

interface PlatformPermissionMapper{
    fun map(permission: AppPermission): List<String>
    fun fromManifestString(manifestString: String) : AppPermission?
}
class AndroidPermissionMapper : PlatformPermissionMapper {

    override fun map(permission: AppPermission): List<String> {
        return when (permission) {
            AppPermission.AudioLibrary -> {
                if(Build.VERSION.SDK_INT >= 33){
                    listOf(Manifest.permission.READ_MEDIA_AUDIO)
                }
                else{
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            AppPermission.Notification -> {
                if(Build.VERSION.SDK_INT >= 33){
                    listOf(Manifest.permission.POST_NOTIFICATIONS)
                }
                else{
                    emptyList()
                }
            }
        }
        }

    override fun fromManifestString(manifestString: String) : AppPermission? {
        return when (manifestString) {
            Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE  -> AppPermission.AudioLibrary
            Manifest.permission.POST_NOTIFICATIONS -> AppPermission.Notification
            else -> null // Handle unknown permissions gracefully
        }
    }

}

interface PermissionDescriptionProvider {
    fun getDescriptionRes(isPermanentlyDeclined: Boolean): Int
}

class AudioDescriptionProvider : PermissionDescriptionProvider {
    override fun getDescriptionRes(isPermanentlyDeclined: Boolean): Int {
        return if (isPermanentlyDeclined) R.string.perm_audio_denied else R.string.perm_audio_rationale
    }
}

class NotificationDescriptionProvider : PermissionDescriptionProvider {
    override fun getDescriptionRes(isPermanentlyDeclined: Boolean): Int {
        return if (isPermanentlyDeclined) R.string.perm_notification_denied else R.string.perm_notification_rationale
    }
}