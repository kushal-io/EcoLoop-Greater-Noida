package com.ecoloop.greaternoida.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.model.UserProfile
import com.ecoloop.greaternoida.ui.aiscanner.AiScannerScreen
import com.ecoloop.greaternoida.ui.aiscanner.AiScannerViewModel
import com.ecoloop.greaternoida.ui.locator.LocatorScreen
import com.ecoloop.greaternoida.ui.locator.LocatorViewModel
import com.ecoloop.greaternoida.ui.profile.ProfileScreen
import com.ecoloop.greaternoida.ui.qr.PartnerQrCodeScreen
import com.ecoloop.greaternoida.ui.qr.QrScannerScreen
import com.ecoloop.greaternoida.ui.rewards.RewardsScreen
import com.ecoloop.greaternoida.ui.rewards.RewardsViewModel
import com.ecoloop.greaternoida.ui.transaction.TransactionHistoryScreen
import com.ecoloop.greaternoida.ui.transaction.TransactionViewModel
import com.ecoloop.greaternoida.ui.transaction.components.DropOffInitiateDialog

@Composable
fun UserHomeScreen(
    user: UserProfile,
    locatorViewModel: LocatorViewModel,
    transactionViewModel: TransactionViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    rewardsViewModel: RewardsViewModel = hiltViewModel(),
    aiScannerViewModel: AiScannerViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isScannerOpen by remember { mutableStateOf(false) }
    var isAiScannerOpen by remember { mutableStateOf(false) }
    var partnerForQrDisplay by remember { mutableStateOf<Partner?>(null) }

    val txState by transactionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(user.userId) {
        transactionViewModel.loadUserTransactions(user.userId)
    }

    LaunchedEffect(txState.successMessage) {
        txState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            transactionViewModel.clearMessages()
            selectedTab = 1 // Switch to History tab to see pending drop-off
        }
    }

    LaunchedEffect(txState.errorMessage) {
        txState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            transactionViewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                // 1. Locator Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Locator") },
                    label = { Text("Locator") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // 2. History Tab
                val pendingCount = txState.userTransactions.count { it.isPending }
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        if (pendingCount > 0) {
                            BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                Icon(imageVector = Icons.Default.History, contentDescription = "Drop-offs")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Drop-offs")
                        }
                    },
                    label = { Text("Drop-offs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // 3. Rewards & Wallet Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = "Rewards") },
                    label = { Text("Rewards") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // 4. Profile Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> LocatorScreen(
                    viewModel = locatorViewModel,
                    onInitiateDropOff = { partner ->
                        transactionViewModel.openDropOffDialog(partner)
                    },
                    onScanQrCode = {
                        isScannerOpen = true
                    },
                    onViewQrCode = { partner ->
                        partnerForQrDisplay = partner
                    },
                    onOpenAiScanner = {
                        isAiScannerOpen = true
                    }
                )
                1 -> TransactionHistoryScreen(
                    user = user,
                    viewModel = transactionViewModel
                )
                2 -> RewardsScreen(
                    user = user,
                    viewModel = rewardsViewModel
                )
                3 -> ProfileScreen(
                    user = user,
                    onSignOut = onSignOut,
                    onNavigateToRewards = { selectedTab = 2 }
                )
            }
        }
    }

    // QR Code Scanner Overlay
    if (isScannerOpen) {
        QrScannerScreen(
            onQrCodeDetected = { code ->
                isScannerOpen = false
                transactionViewModel.handleScannedQrCode(code)
            },
            onBack = { isScannerOpen = false }
        )
    }

    // AI Waste Scanner Overlay
    if (isAiScannerOpen) {
        AiScannerScreen(
            viewModel = aiScannerViewModel,
            onBack = { isAiScannerOpen = false },
            onProceedToDropOff = { partner, categorySlug ->
                isAiScannerOpen = false
                transactionViewModel.openDropOffDialog(partner)
                transactionViewModel.selectCategory(categorySlug)
            }
        )
    }

    // Partner QR Code Display & Print Overlay
    partnerForQrDisplay?.let { partner ->
        PartnerQrCodeScreen(
            initialPartner = partner,
            onBack = { partnerForQrDisplay = null }
        )
    }

    // Drop-off Initiate Dialog
    if (txState.selectedPartnerForDropOff != null) {
        DropOffInitiateDialog(
            partner = txState.selectedPartnerForDropOff!!,
            categories = txState.wasteCategories,
            selectedCategoryId = txState.selectedCategoryId,
            onCategorySelected = { categoryId ->
                transactionViewModel.selectCategory(categoryId)
            },
            onSubmit = {
                transactionViewModel.submitPendingDropOff(user.userId, user.name)
            },
            onDismiss = {
                transactionViewModel.closeDropOffDialog()
            },
            isLoading = txState.isCreatingDropOff
        )
    }
}
