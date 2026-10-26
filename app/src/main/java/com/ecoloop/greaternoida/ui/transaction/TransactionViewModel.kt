package com.ecoloop.greaternoida.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.usecase.ConfirmTransactionUseCase
import com.ecoloop.greaternoida.domain.usecase.CreatePendingTransactionUseCase
import com.ecoloop.greaternoida.domain.usecase.GetPartnerByQrCodeUseCase
import com.ecoloop.greaternoida.domain.usecase.GetPendingTransactionsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetUserTransactionsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetWasteCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val createPendingTransactionUseCase: CreatePendingTransactionUseCase,
    private val confirmTransactionUseCase: ConfirmTransactionUseCase,
    private val getUserTransactionsUseCase: GetUserTransactionsUseCase,
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val getWasteCategoriesUseCase: GetWasteCategoriesUseCase,
    private val getPartnerByQrCodeUseCase: GetPartnerByQrCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadWasteCategories()
    }

    private fun loadWasteCategories() {
        viewModelScope.launch {
            getWasteCategoriesUseCase().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(wasteCategories = result.data) }
                }
            }
        }
    }

    fun loadUserTransactions(userId: String) {
        viewModelScope.launch {
            getUserTransactionsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            userTransactions = result.data,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadPendingTransactionsForPartner(partnerId: String) {
        viewModelScope.launch {
            getPendingTransactionsUseCase(partnerId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingCollectorTransactions = result.data,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun openDropOffDialog(partner: Partner) {
        // Pre-select first accepted waste category
        val firstAccepted = partner.wasteTypesAccepted.firstOrNull() ?: _uiState.value.selectedCategoryId
        _uiState.update {
            it.copy(
                selectedPartnerForDropOff = partner,
                selectedCategoryId = firstAccepted,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun closeDropOffDialog() {
        _uiState.update {
            it.copy(
                selectedPartnerForDropOff = null,
                errorMessage = null
            )
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    /**
     * Resolves a scanned QR Code ID (e.g. "ECO_PARI_001") into a partner and initiates drop-off.
     */
    fun handleScannedQrCode(qrCode: String, onResolved: (Partner) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = getPartnerByQrCodeUseCase(qrCode)) {
                is Resource.Success -> {
                    openDropOffDialog(result.data)
                    onResolved(result.data)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = "Station QR not recognized: \"$qrCode\". ${result.message}")
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitPendingDropOff(userId: String, userName: String) {
        val partner = _uiState.value.selectedPartnerForDropOff ?: return
        val categoryId = _uiState.value.selectedCategoryId

        viewModelScope.launch {
            createPendingTransactionUseCase(
                userId = userId,
                userName = userName,
                partnerId = partner.id,
                partnerName = partner.name,
                categoryId = categoryId
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isCreatingDropOff = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isCreatingDropOff = false,
                                selectedPartnerForDropOff = null,
                                successMessage = "Drop-off submitted at ${partner.name}! Awaiting collector weight confirmation."
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isCreatingDropOff = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun openConfirmDialog(transaction: WasteTransaction) {
        _uiState.update {
            it.copy(
                transactionToConfirm = transaction,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun closeConfirmDialog() {
        _uiState.update { it.copy(transactionToConfirm = null) }
    }

    fun confirmDropOff(transactionId: String, quantityKg: Double, collectorId: String) {
        android.util.Log.d("EcoLoop", "confirmDropOff invoked: tx=$transactionId, qty=$quantityKg kg, collector=$collectorId")
        viewModelScope.launch {
            confirmTransactionUseCase(
                transactionId = transactionId,
                quantityKg = quantityKg,
                collectorId = collectorId
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isConfirming = true) }
                    }
                    is Resource.Success -> {
                        android.util.Log.d("EcoLoop", "confirmDropOff SUCCESS: points=${result.data.pointsAwarded}")
                        _uiState.update {
                            it.copy(
                                isConfirming = false,
                                transactionToConfirm = null,
                                successMessage = "Confirmed ${result.data.quantityKg} kg of ${result.data.categoryId.uppercase()}! Awarded ${result.data.pointsAwarded} points."
                            )
                        }
                    }
                    is Resource.Error -> {
                        android.util.Log.e("EcoLoop", "confirmDropOff ERROR: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isConfirming = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
