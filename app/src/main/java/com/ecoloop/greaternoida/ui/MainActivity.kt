package com.ecoloop.greaternoida.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecoloop.greaternoida.ui.auth.AuthViewModel
import com.ecoloop.greaternoida.ui.locator.LocatorViewModel
import com.ecoloop.greaternoida.ui.theme.EcoLoopTheme
import com.ecoloop.greaternoida.ui.transaction.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoLoopTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val locatorViewModel: LocatorViewModel = hiltViewModel()
                val transactionViewModel: TransactionViewModel = hiltViewModel()

                EcoLoopApp(
                    authViewModel = authViewModel,
                    locatorViewModel = locatorViewModel,
                    transactionViewModel = transactionViewModel
                )
            }
        }
    }
}
