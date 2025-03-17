package com.example.jkconect.main.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.jkconect.ui.theme.RobotoCondensedFontFamily

@Composable
fun FeedScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text(
            text = "Texto Normal",
            fontFamily = RobotoCondensedFontFamily,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Texto em Negrito",
            fontFamily = RobotoCondensedFontFamily,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Texto Leve",
            fontFamily = RobotoCondensedFontFamily,
            fontWeight = FontWeight.Light
        )

    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    FeedScreen()
}