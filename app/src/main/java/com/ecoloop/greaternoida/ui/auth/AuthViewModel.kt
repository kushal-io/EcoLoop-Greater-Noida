package com.ecoloop.greaternoida.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.usecase.GetCurrentUserUseCase
import com.ecoloop.greaternoida.domain.usecase.GetPartnersUseCase
import com.ecoloop.greaternoida.domain.usecase.SignInUseCase
import com.ecoloop.greaternoida.domain.usecase.SignOutUseCase
import com.ecoloop.greaternoida.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPartnersUseCase: GetPartnersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe current user session
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }

        // Load partners for collector assignment dropdown
        viewModelScope.launch {
            getPartnersUseCase().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            availablePartners = result.data,
                            partnerId = currentState.partnerId ?: result.data.firstOrNull()?.id
                        )
                    }
                }
            }
        }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                errorMessage = null
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onSectorChange(sector: String) {
        _uiState.update { it.copy(sector = sector) }
    }

    fun onRoleChange(role: String) {
        _uiState.update {
            it.copy(
                role = role,
                partnerId = if (role == EcoConstants.ROLE_COLLECTOR) {
                    it.partnerId ?: it.availablePartners.firstOrNull()?.id
                } else null
            )
        }
    }

    fun onPartnerChange(partnerId: String) {
        _uiState.update { it.copy(partnerId = partnerId) }
    }

    fun signIn() {
        val state = _uiState.value
        viewModelScope.launch {
            signInUseCase(state.email, state.password).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, currentUser = result.data, errorMessage = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun signUp() {
        val state = _uiState.value
        viewModelScope.launch {
            signUpUseCase(
                name = state.name,
                email = state.email,
                password = state.password,
                sector = state.sector,
                role = state.role,
                partnerId = state.partnerId
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, currentUser = result.data, errorMessage = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun signOut() {
        signOutUseCase()
        _uiState.update {
            it.copy(
                currentUser = null,
                password = "",
                errorMessage = null
            )
        }
    }
}
