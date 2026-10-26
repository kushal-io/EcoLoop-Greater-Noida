package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.UserProfile
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    operator fun invoke(email: String, password: String): Flow<Resource<UserProfile>> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return flow { emit(Resource.Error("Please enter your email address")) }
        }
        if (!EMAIL_REGEX.matches(trimmedEmail)) {
            return flow { emit(Resource.Error("Please enter a valid email address")) }
        }
        if (password.length < 6) {
            return flow { emit(Resource.Error("Password must be at least 6 characters")) }
        }
        return repository.signIn(trimmedEmail, password)
    }
}
