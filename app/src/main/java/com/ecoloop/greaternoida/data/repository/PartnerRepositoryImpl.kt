package com.ecoloop.greaternoida.data.repository

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.datasource.SeedPartnersData
import com.ecoloop.greaternoida.data.model.PartnerDto
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PartnerRepository {

    override fun getPartners(): Flow<Resource<List<Partner>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = firestore.collection(EcoConstants.COLLECTION_PARTNERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fall back to embedded seed data so the prototype locator is always functional
                    val fallback = SeedPartnersData.partners
                    trySend(Resource.Success(fallback))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val partnerList = snapshot.documents.mapNotNull { doc ->
                        val dto = doc.toObject(PartnerDto::class.java)
                        dto?.toDomain(doc.id)
                    }
                    trySend(Resource.Success(partnerList))
                } else {
                    // If Firestore collection is empty, use seed data
                    trySend(Resource.Success(SeedPartnersData.partners))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getPartnerById(partnerId: String): Resource<Partner> {
        return try {
            val doc = firestore.collection(EcoConstants.COLLECTION_PARTNERS)
                .document(partnerId)
                .get()
                .await()

            val partner = doc.toObject(PartnerDto::class.java)?.toDomain(doc.id)
                ?: SeedPartnersData.partners.find { it.id == partnerId }

            if (partner != null) {
                Resource.Success(partner)
            } else {
                Resource.Error("Partner with ID $partnerId not found")
            }
        } catch (e: Exception) {
            val fallback = SeedPartnersData.partners.find { it.id == partnerId }
            if (fallback != null) {
                Resource.Success(fallback)
            } else {
                Resource.Error(e.localizedMessage ?: "Failed to fetch partner details", e)
            }
        }
    }
}
