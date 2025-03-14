package com.example.jkconect.navigation.item

import androidx.annotation.DrawableRes
import com.example.jkconect.R

sealed class BottomNavItem(
    val title: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,

    ) {

    object Feed : BottomNavItem(
        title = R.string.bottom_bar_item_feed,
        selectedIcon = R.drawable.ic_feed_selected,
        unselectedIcon = R.drawable.ic_feed_unselected
    )

    object Calendar : BottomNavItem(
        title = R.string.bottom_bar_item_calendar,
        selectedIcon = R.drawable.ic_calendar_selected,
        unselectedIcon = R.drawable.ic_calendar_unselected
    )

    object MyEvents : BottomNavItem(
        title = R.string.bottom_bar_item_myevents,
        selectedIcon = R.drawable.ic_heart_selected,
        unselectedIcon = R.drawable.ic_heart_unselected
    )

    object Profile : BottomNavItem(
        title = R.string.bottom_bar_item_profile,
        selectedIcon = R.drawable.ic_profile_selected,
        unselectedIcon = R.drawable.ic_profile_unselected
    )

    companion object{
        val items = listOf(Feed, Calendar, MyEvents, Profile)
    }

}