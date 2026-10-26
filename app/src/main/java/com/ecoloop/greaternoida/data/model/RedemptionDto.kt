package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.domain.model.Redemption
import com.google.firebase.firestore.PropertyName

data class RedemptionDto(
    var id: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("reward_id")
    @set:PropertyName("reward_id")
    var rewardId: String = "",

    @get:PropertyName("reward_title")
    @set:PropertyName("reward_title")
    var rewardTitle: String = "",

    @get:PropertyName("partner_name")
    @set:PropertyName("partner_name")
    var partnerName: String = "",

    @get:PropertyName("points_spent")
    @set:PropertyName("points_spent")
    var pointsSpent: Int = 0,

    @get:PropertyName("redemption_code")
    @set:PropertyName("redemption_code")
    var redemptionCode: String = "",

    @get:PropertyName("discount_text")
    @set:PropertyName("discount_text")
    var discountText: String = "",

    @get:PropertyName("redeemed_at")
    @set:PropertyName("redeemed_at")
    var redeemedAt: Long = System.currentTimeMillis(),

    var status: String = "active"
) {
    fun toDomain(documentId: String = id): Redemption {
        return Redemption(
            id = documentId.ifBlank { id },
            userId = userId,
            rewardId = rewardId,
            rewardTitle = rewardTitle,
            partnerName = partnerName,
            pointsSpent = pointsSpent,
            redemptionCode = redemptionCode,
            discountText = discountText,
            redeemedAt = redeemedAt,
            status = status
        )
    }

    companion object {
        fun fromDomain(redemption: Redemption): RedemptionDto {
            return RedemptionDto(
                id = redemption.id,
                userId = redemption.userId,
                rewardId = redemption.rewardId,
                rewardTitle = redemption.rewardTitle,
                partnerName = redemption.partnerName,
                pointsSpent = redemption.pointsSpent,
                redemptionCode = redemption.redemptionCode,
                discountText = redemption.discountText,
                redeemedAt = redemption.redeemedAt,
                status = redemption.status
            )
        }
    }
}
