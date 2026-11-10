package com.familyguard.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.familyguard.calls.ui.GuardApp
import com.familyguard.calls.ui.GuardTheme

/** The UI follows the phone language: Arabic (right-to-left) or English (left-to-right). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GuardTheme {
                GuardApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        KeepAliveService.startIfEnabled(this)
    }
}
