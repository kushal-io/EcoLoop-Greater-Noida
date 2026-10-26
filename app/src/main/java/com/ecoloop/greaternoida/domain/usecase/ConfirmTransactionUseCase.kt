package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ConfirmTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        transactionId: String,
        quantityKg: Double,
        collectorId: String
    ): Flow<Resource<WasteTransaction>> {
        if (transactionId.isBlank()) {
            return flow { emit(Resource.Error("Invalid transaction reference")) }
        }
        if (quantityKg <= 0.0) {
            return flow { emit(Resource.Error("Quantity must be greater than 0 kg")) }
        }
        if (quantityKg > 500.0) {
            return flow { emit(Resource.Error("Single drop-off cannot exceed 500 kg")) }
        }
        if (collectorId.isBlank()) {
            return flow { emit(Resource.Error("Collector authentication required")) }
        }

        return repository.confirmTransaction(
            transactionId = transactionId,
            quantityKg = quantityKg,
            collectorId = collectorId
        )
    }
}
