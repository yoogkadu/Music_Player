package com.example.musicplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.musicplayer.data.BootstrapState
import com.example.musicplayer.nav.Routes
import com.example.musicplayer.ui.viewModels.BootStrapViewModel

@Composable
fun BootUpScreen(navController: NavHostController,viewModel: BootStrapViewModel){

        
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {

}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier,message: String) {

}