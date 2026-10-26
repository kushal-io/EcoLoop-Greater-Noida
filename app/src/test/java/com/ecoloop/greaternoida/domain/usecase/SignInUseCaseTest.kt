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

class SignInUseCaseTest {

    private lateinit var signInUseCase: SignInUseCase
    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<UserProfile?> = flow { emit(null) }

        override fun signIn(email: String, password: String): Flow<Resource<UserProfile>> = flow {
            emit(
                Resource.Success(
                    UserProfile(
                        userId = "signed_in_user_id",
                        email = email,
                        name = "Aarav Sharma",
                        sector = "Alpha 1",
                        role = EcoConstants.ROLE_USER
                    )
                )
            )
        }

        override fun signUp(
            name: String,
            email: String,
            password: String,
            sector: String,
            role: String,
            partnerId: String?
        ): Flow<Resource<UserProfile>> = flow {
            emit(Resource.Success(UserProfile(email = email)))
        }

        override fun signOut() {}

        override suspend fun getCurrentUserProfile(): Resource<UserProfile?> {
            return Resource.Success(null)
        }
    }

    @Before
    fun setUp() {
        signInUseCase = SignInUseCase(fakeAuthRepository)
    }

    @Test
    fun `blank email fails validation`() = runTest {
        val result = signInUseCase("", "password123").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("email")
    }

    @Test
    fun `invalid email fails validation`() = runTest {
        val result = signInUseCase("not-an-email", "password123").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("valid email")
    }

    @Test
    fun `short password fails validation`() = runTest {
        val result = signInUseCase("aarav@ecoloop.in", "123").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("6 characters")
    }

    @Test
    fun `valid credentials succeed`() = runTest {
        val result = signInUseCase("aarav@ecoloop.in", "password123").first()
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val profile = (result as Resource.Success).data
        assertThat(profile.email).isEqualTo("aarav@ecoloop.in")
    }
}
