package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.domain.model.UserProfile
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<UserProfile?> {
        return repository.currentUser
    }
}
