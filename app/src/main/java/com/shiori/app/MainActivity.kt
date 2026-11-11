package com.shiori.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shiori.app.ui.navigation.ShioriNavHost
import com.shiori.app.ui.theme.ShioriTheme
import javax.inject.Inject
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IReminderRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gameRepository: IGameRepository

    @Inject
    lateinit var reminderRepository: IReminderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShioriTheme {
                ShioriNavHost(
                    gameRepository = gameRepository,
                    reminderRepository = reminderRepository
                )
            }
        }
    }
}
