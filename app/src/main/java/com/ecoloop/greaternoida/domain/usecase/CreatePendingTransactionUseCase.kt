package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreatePendingTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        userId: String,
        userName: String,
        partnerId: String,
        partnerName: String,
        categoryId: String
    ): Flow<Resource<WasteTransaction>> {
        if (userId.isBlank()) {
            return flow { emit(Resource.Error("User must be signed in to create drop-off")) }
        }
        if (partnerId.isBlank()) {
            return flow { emit(Resource.Error("Please select a valid drop-off station")) }
        }
        if (categoryId.isBlank()) {
            return flow { emit(Resource.Error("Please select a waste category")) }
        }

        return repository.createPendingTransaction(
            userId = userId,
            userName = userName.ifBlank { "Citizen" },
            partnerId = partnerId,
            partnerName = partnerName,
            categoryId = categoryId
        )
    }
}
