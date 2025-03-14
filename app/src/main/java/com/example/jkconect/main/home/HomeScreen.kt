import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jkconect.navigation.item.BottomNavItem
import com.example.jkconect.navigation.navhost.BottomAppBarNavHost

@Composable
fun HomeScreen(navHostController: NavHostController = rememberNavController()) {
    Scaffold(

        bottomBar = {
            NavigationBar (
                containerColor = Color.Black,
                content = {
                    BottomNavItem.items.forEachIndexed{ index, item ->

                    }
                },
            )
        },
        floatingActionButton = {

        },
        topBar = {

        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
            ){
                BottomAppBarNavHost(navHostController)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}