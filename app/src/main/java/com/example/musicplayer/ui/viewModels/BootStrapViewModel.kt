package com.example.musicplayer.ui.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.musicplayer.data.BootstrapState
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.permission.AppPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BootStrapViewModel(
    val musicRepository: MusicRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow<BootstrapState>(BootstrapState.NeedsPermission)
    val uiState= _uiState.asStateFlow()

    val visiblePermissionDialogQueue= mutableStateListOf<AppPermission>()

    fun dismissDialog(){
        visiblePermissionDialogQueue.removeFirstOrNull()

    }
    fun onPermissionResult(
        permission: AppPermission,
        isGranted: Boolean
    ) {
        if(!isGranted){
            visiblePermissionDialogQueue.add(permission)
        }


    }

}