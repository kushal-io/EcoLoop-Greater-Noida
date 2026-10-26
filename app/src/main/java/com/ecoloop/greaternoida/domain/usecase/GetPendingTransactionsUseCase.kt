package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(partnerId: String): Flow<Resource<List<WasteTransaction>>> {
        return repository.getPendingTransactionsForPartner(partnerId)
    }
}
