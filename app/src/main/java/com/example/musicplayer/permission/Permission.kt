package com.example.musicplayer.permission

import android.Manifest
import com.example.musicplayer.R

enum class AppPermission {
    READ_AUDIO,
    POST_NOTIFICATIONS,
    READ_EXTERNAL_STORAGE,
}
fun String.toAppPermission(): AppPermission? {
    return when (this) {
        Manifest.permission.RECORD_AUDIO -> AppPermission.READ_AUDIO
        Manifest.permission.POST_NOTIFICATIONS -> AppPermission.POST_NOTIFICATIONS
        Manifest.permission.READ_EXTERNAL_STORAGE -> AppPermission.READ_EXTERNAL_STORAGE
        else -> null // Handle unknown permissions gracefully
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