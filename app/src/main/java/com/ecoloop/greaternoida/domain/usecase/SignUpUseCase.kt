package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.UserProfile
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    operator fun invoke(
        name: String,
        email: String,
        password: String,
        sector: String,
        role: String,
        partnerId: String? = null
    ): Flow<Resource<UserProfile>> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedSector = sector.trim()

        if (trimmedName.isBlank()) {
            return flow { emit(Resource.Error("Please enter your name")) }
        }
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return flow { emit(Resource.Error("Please enter a valid email address")) }
        }
        if (password.length < 6) {
            return flow { emit(Resource.Error("Password must be at least 6 characters")) }
        }
        if (trimmedSector.isBlank()) {
            return flow { emit(Resource.Error("Please select your Greater Noida sector")) }
        }
        if (role == EcoConstants.ROLE_COLLECTOR && partnerId.isNullOrBlank()) {
            return flow { emit(Resource.Error("Collectors must select an affiliated drop-off location")) }
        }

        return repository.signUp(
            name = trimmedName,
            email = trimmedEmail,
            password = password,
            sector = trimmedSector,
            role = role,
            partnerId = if (role == EcoConstants.ROLE_COLLECTOR) partnerId else null
        )
    }
}
