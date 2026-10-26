package com.ecoloop.greaternoida.ui.transaction

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.model.WasteCategory
import com.ecoloop.greaternoida.domain.model.WasteTransaction

data class TransactionUiState(
    val wasteCategories: List<WasteCategory> = emptyList(),
    val userTransactions: List<WasteTransaction> = emptyList(),
    val pendingCollectorTransactions: List<WasteTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingDropOff: Boolean = false,
    val isConfirming: Boolean = false,
    val selectedPartnerForDropOff: Partner? = null,
    val selectedCategoryId: String = EcoConstants.WASTE_TYPE_PLASTIC,
    val transactionToConfirm: WasteTransaction? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
