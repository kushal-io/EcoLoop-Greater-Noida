package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() {
        repository.signOut()
    }
}
