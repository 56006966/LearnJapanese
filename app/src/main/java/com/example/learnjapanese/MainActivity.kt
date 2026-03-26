package com.example.learnjapanese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.learnjapanese.ui.LearnJapaneseApp
import com.example.learnjapanese.ui.theme.LearnJapaneseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnJapaneseTheme {
                LearnJapaneseApp()
            }
        }
    }
}
