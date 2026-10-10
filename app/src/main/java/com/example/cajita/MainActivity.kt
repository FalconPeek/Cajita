package com.example.cajita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cajita.ui.navigation.CajitaNavHost
import com.example.cajita.ui.theme.CajitaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CajitaTheme {
                CajitaNavHost()
            }
        }
    }
}
