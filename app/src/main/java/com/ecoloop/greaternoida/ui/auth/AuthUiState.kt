package com.ecoloop.greaternoida.ui.auth

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.model.UserProfile

data class AuthUiState(
    val isSignUpMode: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val sector: String = "Pari Chowk",
    val role: String = EcoConstants.ROLE_USER,
    val partnerId: String? = null,
    val availablePartners: List<Partner> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: UserProfile? = null
) {
    val isCollector: Boolean
        get() = role == EcoConstants.ROLE_COLLECTOR
}
