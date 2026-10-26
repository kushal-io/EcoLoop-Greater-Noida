package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<WasteTransaction>>> {
        return repository.getUserTransactions(userId)
    }
}
