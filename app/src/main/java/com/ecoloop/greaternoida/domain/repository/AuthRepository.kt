package com.ecoloop.greaternoida.domain.repository

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>

    fun signIn(email: String, password: String): Flow<Resource<UserProfile>>

    fun signUp(
        name: String,
        email: String,
        password: String,
        sector: String,
        role: String,
        partnerId: String? = null
    ): Flow<Resource<UserProfile>>

    fun signOut()

    suspend fun getCurrentUserProfile(): Resource<UserProfile?>

    fun updateUserPoints(pointsDelta: Int, kgDelta: Double = 0.0)
}
