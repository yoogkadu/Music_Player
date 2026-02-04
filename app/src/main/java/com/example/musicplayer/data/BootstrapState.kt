package com.example.musicplayer.data

sealed class BootStrapState {
    object NeedsPermission: BootStrapState()
    object Loading : BootStrapState()
    object Ready : BootStrapState()
    data class Error(val message: String) : BootStrapState()
}