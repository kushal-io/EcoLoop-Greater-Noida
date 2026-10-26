package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.domain.model.Reward
import com.google.firebase.firestore.PropertyName

data class RewardDto(
    var id: String = "",
    var title: String = "",
    var description: String = "",

    @get:PropertyName("partner_name")
    @set:PropertyName("partner_name")
    var partnerName: String = "",

    @get:PropertyName("points_cost")
    @set:PropertyName("points_cost")
    var pointsCost: Int = 0,

    var category: String = "",

    @get:PropertyName("discount_text")
    @set:PropertyName("discount_text")
    var discountText: String = "",

    var terms: String = "",
    var available: Boolean = true
) {
    fun toDomain(documentId: String = id): Reward {
        return Reward(
            id = documentId.ifBlank { id },
            title = title,
            description = description,
            partnerName = partnerName,
            pointsCost = pointsCost,
            category = category,
            discountText = discountText,
            terms = terms,
            available = available
        )
    }

    companion object {
        fun fromDomain(reward: Reward): RewardDto {
            return RewardDto(
                id = reward.id,
                title = reward.title,
                description = reward.description,
                partnerName = reward.partnerName,
                pointsCost = reward.pointsCost,
                category = reward.category,
                discountText = reward.discountText,
                terms = reward.terms,
                available = reward.available
            )
        }
    }
}
