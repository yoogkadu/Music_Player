package com.example.musicplayer.ui.viewModels

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.musicplayer.data.BootStrapState
import com.example.musicplayer.permission.AppPermission
import com.example.musicplayer.permission.PlatformPermissionMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class BootStrapViewModel(
    val mapper: PlatformPermissionMapper,
    val application : Application

) : ViewModel(){
    private val _uiState = MutableStateFlow<BootStrapState>(BootStrapState.NeedsPermission)
    val uiState= _uiState.asStateFlow()
    val visiblePermissionDialogQueue= mutableStateListOf<AppPermission>()


    fun dismissDialog(){
        visiblePermissionDialogQueue.removeFirstOrNull()
        checkIfReadyToProceed()
    }
    fun getAllRequiredPermissions(): Array<String> {
        return mapper.allPermissions
            .flatMap { mapper.map(it) }
            .toTypedArray()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        val appPerm = mapper.fromManifestString(permission) ?: return
        visiblePermissionDialogQueue.remove(appPerm)

        if(!isGranted ){
            if (!visiblePermissionDialogQueue.contains(appPerm)) {
                visiblePermissionDialogQueue.add(appPerm)
            }
        }
        if (isGranted || appPerm.isOptional) {
            checkIfReadyToProceed()
        }


    }
     fun checkIfReadyToProceed() {
        // 1. Get a list of all permissions that are REQUIRED (not optional)
        if (_uiState.value != BootStrapState.NeedsPermission) return
        val crucialPermissions = mapper.allPermissions
            .filter { !it.isOptional }

        // 2. Check if the gateway says they are all granted
        val allCrucialGranted = crucialPermissions.all { permission ->
            hasPermission(permission)
        }
         if (visiblePermissionDialogQueue.isNotEmpty()) {
             return
         }

        // 3. If everything crucial is good, move to Loading
        if (allCrucialGranted) {
            _uiState.value = BootStrapState.Loading
             // Trigger the repository to scan files
        }
    }
    private fun hasPermission(permission: AppPermission): Boolean {
        val manifestStrings = mapper.map(permission)

        // If no strings needed (like old Android Notifications), it's "granted"
        if (manifestStrings.isEmpty()) return true

        return manifestStrings.all { manifestString ->
            ContextCompat.checkSelfPermission(
                application,
                manifestString
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}