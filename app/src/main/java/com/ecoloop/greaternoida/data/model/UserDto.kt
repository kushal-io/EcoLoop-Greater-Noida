package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.UserProfile
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    var name: String = "",
    var email: String = "",
    var sector: String = "",
    var role: String = EcoConstants.ROLE_USER,

    @get:PropertyName("partner_id")
    @set:PropertyName("partner_id")
    var partnerId: String? = null,

    @get:PropertyName("total_points")
    @set:PropertyName("total_points")
    var totalPoints: Int = 0,

    @get:PropertyName("total_kg_recycled")
    @set:PropertyName("total_kg_recycled")
    var totalKgRecycled: Double = 0.0,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(documentId: String = userId): UserProfile {
        return UserProfile(
            userId = documentId.ifBlank { userId },
            email = email,
            name = name,
            sector = sector,
            role = role,
            partnerId = partnerId,
            totalPoints = totalPoints,
            totalKgRecycled = totalKgRecycled,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(userProfile: UserProfile): UserDto {
            return UserDto(
                userId = userProfile.userId,
                name = userProfile.name,
                email = userProfile.email,
                sector = userProfile.sector,
                role = userProfile.role,
                partnerId = userProfile.partnerId,
                totalPoints = userProfile.totalPoints,
                totalKgRecycled = userProfile.totalKgRecycled,
                createdAt = userProfile.createdAt
            )
        }
    }
}
