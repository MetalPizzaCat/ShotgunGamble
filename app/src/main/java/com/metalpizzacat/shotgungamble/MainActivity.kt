package com.metalpizzacat.shotgungamble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.metalpizzacat.shotgungamble.components.GameDisplay
import com.metalpizzacat.shotgungamble.ui.theme.ShotgungambleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShotgungambleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameDisplay(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
