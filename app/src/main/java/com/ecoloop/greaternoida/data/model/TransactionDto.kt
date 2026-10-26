package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.google.firebase.firestore.PropertyName

data class TransactionDto(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("user_name")
    @set:PropertyName("user_name")
    var userName: String = "",

    @get:PropertyName("partner_id")
    @set:PropertyName("partner_id")
    var partnerId: String = "",

    @get:PropertyName("partner_name")
    @set:PropertyName("partner_name")
    var partnerName: String = "",

    @get:PropertyName("category_id")
    @set:PropertyName("category_id")
    var categoryId: String = "",

    @get:PropertyName("quantity_kg")
    @set:PropertyName("quantity_kg")
    var quantityKg: Double = 0.0,

    var status: String = EcoConstants.STATUS_PENDING,

    @get:PropertyName("points_awarded")
    @set:PropertyName("points_awarded")
    var pointsAwarded: Int = 0,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("confirmed_at")
    @set:PropertyName("confirmed_at")
    var confirmedAt: Long? = null,

    @get:PropertyName("collector_id")
    @set:PropertyName("collector_id")
    var collectorId: String? = null
) {
    fun toDomain(documentId: String): WasteTransaction {
        return WasteTransaction(
            id = documentId,
            userId = userId,
            userName = userName,
            partnerId = partnerId,
            partnerName = partnerName,
            categoryId = categoryId,
            quantityKg = quantityKg,
            status = status,
            pointsAwarded = pointsAwarded,
            createdAt = createdAt,
            confirmedAt = confirmedAt,
            collectorId = collectorId
        )
    }

    companion object {
        fun fromDomain(transaction: WasteTransaction): TransactionDto {
            return TransactionDto(
                userId = transaction.userId,
                userName = transaction.userName,
                partnerId = transaction.partnerId,
                partnerName = transaction.partnerName,
                categoryId = transaction.categoryId,
                quantityKg = transaction.quantityKg,
                status = transaction.status,
                pointsAwarded = transaction.pointsAwarded,
                createdAt = transaction.createdAt,
                confirmedAt = transaction.confirmedAt,
                collectorId = transaction.collectorId
            )
        }
    }
}
