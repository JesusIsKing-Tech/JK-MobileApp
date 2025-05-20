package com.example.jkconect

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleLoginTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun login_sucesso_para_perfil() {
        composeTestRule.onNodeWithTag("login_email_input")
            .performTextInput("a@gmail.com")

        composeTestRule.onNodeWithTag("login_password_input")
            .performTextInput("Gustavo2430@")

        composeTestRule.onNodeWithTag("login_button")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                // tag na tela de perfil
                composeTestRule.onNodeWithTag("tela_home").isDisplayed()
            } catch (e: Exception) {
                false
            }
        }

        composeTestRule.onNodeWithTag("titulo_bem_vindo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subtitulo_bem_vindo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("barra_pesquisa").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ver_todos_eventos").assertIsDisplayed()


        //elementos de login nao visiveis
        composeTestRule.onNodeWithTag("login_email_input").assertDoesNotExist()
        composeTestRule.onNodeWithTag("login_password_input").assertDoesNotExist()

        //espera apenas para a visualizacao nossa
        Thread.sleep(4000)
    }
}