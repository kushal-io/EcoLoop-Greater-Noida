package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.UserProfile
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignUpUseCaseTest {

    private lateinit var signUpUseCase: SignUpUseCase
    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<UserProfile?> = flow { emit(null) }

        override fun signIn(email: String, password: String): Flow<Resource<UserProfile>> = flow {
            emit(Resource.Success(UserProfile(email = email)))
        }

        override fun signUp(
            name: String,
            email: String,
            password: String,
            sector: String,
            role: String,
            partnerId: String?
        ): Flow<Resource<UserProfile>> = flow {
            emit(
                Resource.Success(
                    UserProfile(
                        userId = "test_user_id",
                        name = name,
                        email = email,
                        sector = sector,
                        role = role,
                        partnerId = partnerId
                    )
                )
            )
        }

        override fun signOut() {}

        override suspend fun getCurrentUserProfile(): Resource<UserProfile?> {
            return Resource.Success(null)
        }
    }

    @Before
    fun setUp() {
        signUpUseCase = SignUpUseCase(fakeAuthRepository)
    }

    @Test
    fun `blank name fails validation`() = runTest {
        val result = signUpUseCase(
            name = "",
            email = "user@test.com",
            password = "password123",
            sector = "Alpha 1",
            role = EcoConstants.ROLE_USER
        ).first()

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("name")
    }

    @Test
    fun `short password fails validation`() = runTest {
        val result = signUpUseCase(
            name = "Aarav Sharma",
            email = "aarav@ecoloop.in",
            password = "123",
            sector = "Alpha 1",
            role = EcoConstants.ROLE_USER
        ).first()

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("6 characters")
    }

    @Test
    fun `collector role without partnerId fails validation`() = runTest {
        val result = signUpUseCase(
            name = "Collector Mohan",
            email = "mohan@ecoloop.in",
            password = "password123",
            sector = "Pari Chowk",
            role = EcoConstants.ROLE_COLLECTOR,
            partnerId = null
        ).first()

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("drop-off location")
    }

    @Test
    fun `valid citizen sign up succeeds with role user`() = runTest {
        val result = signUpUseCase(
            name = "Aarav Sharma",
            email = "aarav@ecoloop.in",
            password = "password123",
            sector = "Alpha 1",
            role = EcoConstants.ROLE_USER,
            partnerId = null
        ).first()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val profile = (result as Resource.Success).data
        assertThat(profile.name).isEqualTo("Aarav Sharma")
        assertThat(profile.role).isEqualTo(EcoConstants.ROLE_USER)
        assertThat(profile.isCollector).isFalse()
    }

    @Test
    fun `valid collector sign up succeeds with role collector and assigned partnerId`() = runTest {
        val result = signUpUseCase(
            name = "Mohan Lal",
            email = "mohan@ecoloop.in",
            password = "password123",
            sector = "Pari Chowk",
            role = EcoConstants.ROLE_COLLECTOR,
            partnerId = "partner_pari_01"
        ).first()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val profile = (result as Resource.Success).data
        assertThat(profile.role).isEqualTo(EcoConstants.ROLE_COLLECTOR)
        assertThat(profile.isCollector).isTrue()
        assertThat(profile.partnerId).isEqualTo("partner_pari_01")
    }
}
