package com.example.musicplayer.data

sealed class BootstrapState {
    object NeedsPermission: BootstrapState()
    object Loading : BootstrapState()
    object Ready : BootstrapState()
    data class Error(val message: String) : BootstrapState()
}