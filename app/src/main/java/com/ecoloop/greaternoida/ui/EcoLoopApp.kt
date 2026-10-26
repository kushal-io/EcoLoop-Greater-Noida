package com.ecoloop.greaternoida.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ecoloop.greaternoida.ui.auth.AuthScreen
import com.ecoloop.greaternoida.ui.auth.AuthViewModel
import com.ecoloop.greaternoida.ui.home.CollectorHomeScreen
import com.ecoloop.greaternoida.ui.home.UserHomeScreen
import com.ecoloop.greaternoida.ui.locator.LocatorViewModel
import com.ecoloop.greaternoida.ui.transaction.TransactionViewModel

@Composable
fun EcoLoopApp(
    authViewModel: AuthViewModel,
    locatorViewModel: LocatorViewModel,
    transactionViewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser = authState.currentUser

    Crossfade(
        targetState = currentUser,
        label = "AuthRoutingCrossfade",
        modifier = modifier.fillMaxSize()
    ) { user ->
        when {
            user == null -> {
                AuthScreen(viewModel = authViewModel)
            }
            user.isCollector -> {
                CollectorHomeScreen(
                    user = user,
                    transactionViewModel = transactionViewModel,
                    onSignOut = { authViewModel.signOut() }
                )
            }
            else -> {
                UserHomeScreen(
                    user = user,
                    locatorViewModel = locatorViewModel,
                    transactionViewModel = transactionViewModel,
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }
    }
}
