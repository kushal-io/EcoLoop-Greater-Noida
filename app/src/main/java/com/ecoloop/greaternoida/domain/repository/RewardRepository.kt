package com.ecoloop.greaternoida.domain.repository

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.model.Reward
import kotlinx.coroutines.flow.Flow

interface RewardRepository {
    /**
     * Streams the catalog of active rewards from Firestore (with local seed fallback).
     */
    fun getRewards(): Flow<Resource<List<Reward>>>

    /**
     * Streams all rewards redeemed by the given user.
     */
    fun getUserRedemptions(userId: String): Flow<Resource<List<Redemption>>>

    /**
     * Atomically executes reward redemption using a Firestore transaction:
     * 1. Checks that the user has sufficient points (points >= reward.pointsCost)
     * 2. Prevents any negative balance
     * 3. Deducts points from user document
     * 4. Creates a new redemption document with a unique short redemption code
     */
    fun redeemReward(userId: String, reward: Reward): Flow<Resource<Redemption>>
}
