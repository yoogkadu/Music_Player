package com.example.musicplayer.ui.bottomNavigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicplayer.nav.HomeScreenRoute


@Composable
fun BottomNavBar(modifier: Modifier = Modifier,
                 navigationList: List<HomeScreenRoute>,
                 onClick : (HomeScreenRoute) -> Unit = {},
                 currentItem: HomeScreenRoute = HomeScreenRoute.Search
) {
   NavigationBar(
       modifier = modifier
   ) {
       navigationList.forEach {
           item->
           NavigationBarItem(
               selected = item == currentItem,
               onClick = {
                   onClick(item)
               },
               icon = { Icon(painterResource(item.iconId),
                   contentDescription = stringResource(item.stringId))
                   },

               modifier = Modifier,
               label = {Text(stringResource(item.stringId))}

           )
       }

   }

}

@Preview
@Composable
private fun BottomNavBarPreview() {
    BottomNavBar(navigationList = HomeScreenRoute.routes)
}