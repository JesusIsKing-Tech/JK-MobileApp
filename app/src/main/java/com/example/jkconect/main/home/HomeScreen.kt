
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jkconect.main.calendar.CalendarScreen
import com.example.jkconect.navigation.item.BottomNavItem
import com.example.jkconect.navigation.navhost.BottomAppBarNavHost
import com.example.jkconect.navigation.navhost.CalendarScreenRoute
import com.example.jkconect.navigation.navhost.FeedScreenRoute
import com.example.jkconect.navigation.navhost.MyEventsScreenRoute
import com.example.jkconect.navigation.navhost.ProfileScreenRoute
import com.example.jkconect.ui.theme.AlphaPrimaryColor
import com.example.jkconect.ui.theme.CinzaEscuroFundo
import com.example.jkconect.ui.theme.PrimaryColor

@Composable
fun HomeScreen(navHostController: NavHostController = rememberNavController()) {

    var selectedItem by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        containerColor = CinzaEscuroFundo,
        bottomBar = {
            NavigationBar (
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .border(1.dp, CinzaEscuroFundo, CircleShape),
                containerColor = Color.Transparent,

                content = {
                    BottomNavItem.items.forEachIndexed{ index, item ->
                        if(index == 0){
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        AddBottomItem(
                            item = item,
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index

                                when(index) {
                                    0 -> navHostController.navigate(FeedScreenRoute)
                                    1 -> navHostController.navigate(CalendarScreenRoute)
                                    2 -> navHostController.navigate(MyEventsScreenRoute)
                                    3 -> navHostController.navigate(ProfileScreenRoute)
                                }
                            },
                            /*
                            Esse trecho serve para mostrar o nome abaixo dos icons se estiverem selecionados

                            label = {
                                if (selectedItem == index){
                                    Text(
                                        text = stringResource(id= item.title),
                                        style = TextStyle(
                                            color = PrimaryColor,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedItem == index){
                                                FontWeight.Bold
                                            } else{
                                                FontWeight.Normal
                                            }
                                        )
                                    )
                                }
                            }

                           */
                        )

                        if(index == BottomNavItem.items.size -1){
                            Spacer(modifier = Modifier.width(8.dp))
                        }
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

@Composable
fun RowScope.AddBottomItem(
    item: BottomNavItem,
    selected: Boolean,
    label: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick= onClick,
        label= label,
        icon = {
            Icon(
                painter = painterResource(id= if(selected) item.selectedIcon else item.unselectedIcon),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 5.dp)
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryColor,
            unselectedIconColor = PrimaryColor,
            indicatorColor = AlphaPrimaryColor
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}