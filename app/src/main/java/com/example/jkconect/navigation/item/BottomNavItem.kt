package com.example.jkconect.navigation.item

import androidx.annotation.DrawableRes
import com.example.jkconect.R

// ESTILIZAR A NAV na main activity

sealed class BottomNavItem(
    val route: String,
    val title: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) {
    object Home : BottomNavItem(
        route = HomeScreenRoute.route,
        title = R.string.bottom_bar_item_feed,
        selectedIcon = R.drawable.ic_feed_selected,
        unselectedIcon = R.drawable.ic_feed_unselected
    )

    object Calendar : BottomNavItem(
        route = CalendarScreenRoute.route,
        title = R.string.bottom_bar_item_calendar,
        selectedIcon = R.drawable.ic_calendar_selected,
        unselectedIcon = R.drawable.ic_calendar_unselected
    )

    object MyEvents : BottomNavItem(
        route = MyEventsScreenRoute.route,
        title = R.string.bottom_bar_item_myevents,
        selectedIcon = R.drawable.ic_heart_selected,
        unselectedIcon = R.drawable.ic_heart_unselected
    )

    object Profile : BottomNavItem(
        route = ProfileScreenRoute.route,
        title = R.string.bottom_bar_item_profile,
        selectedIcon = R.drawable.ic_profile_selected,
        unselectedIcon = R.drawable.ic_profile_unselected
    )

    companion object {
        val items = listOf(Home, Calendar, MyEvents, Profile)
    }
}

object CalendarScreenRoute {
    const val route = "calendar"
}

object MyEventsScreenRoute {
    const val route = "myevents" // Correção: sem sublinhado para consistência
}

object ProfileScreenRoute {
    const val route = "profile"
}

object HomeScreenRoute {
    const val route = "home"
}

object LoginScreenRoute {
    const val route = "login"
}