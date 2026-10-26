package com.ecoloop.greaternoida.domain.repository

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteCategory
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    /**
     * Creates a new pending recycling drop-off initiated by a user.
     */
    fun createPendingTransaction(
        userId: String,
        userName: String,
        partnerId: String,
        partnerName: String,
        categoryId: String
    ): Flow<Resource<WasteTransaction>>

    /**
     * Streams transactions created by the specified user (history).
     */
    fun getUserTransactions(userId: String): Flow<Resource<List<WasteTransaction>>>

    /**
     * Streams all pending transactions awaiting confirmation at a specific partner station.
     */
    fun getPendingTransactionsForPartner(partnerId: String): Flow<Resource<List<WasteTransaction>>>

    /**
     * Confirms a transaction by recording measured quantity in kg,
     * calculating points (quantity_kg * points_per_kg * hazard_weight),
     * and atomically updating user total_points via a Firestore transaction to prevent double crediting.
     */
    fun confirmTransaction(
        transactionId: String,
        quantityKg: Double,
        collectorId: String
    ): Flow<Resource<WasteTransaction>>

    /**
     * Gets waste categories with current point rates and hazard weights.
     */
    fun getWasteCategories(): Flow<Resource<List<WasteCategory>>>
}
