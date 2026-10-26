package com.ecoloop.greaternoida.domain.repository

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import kotlinx.coroutines.flow.Flow

interface PartnerRepository {
    /**
     * Streams the list of recycling partners from Firestore.
     * Falls back to bundled realistic seed partners if Firestore is empty or offline.
     */
    fun getPartners(): Flow<Resource<List<Partner>>>

    /**
     * Gets a single partner by ID.
     */
    suspend fun getPartnerById(partnerId: String): Resource<Partner>
}
