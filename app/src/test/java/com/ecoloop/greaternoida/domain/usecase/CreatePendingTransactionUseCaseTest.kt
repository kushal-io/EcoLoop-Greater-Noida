package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.WasteCategory
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreatePendingTransactionUseCaseTest {

    private lateinit var createPendingTransactionUseCase: CreatePendingTransactionUseCase

    private val fakeRepository = object : TransactionRepository {
        override fun createPendingTransaction(
            userId: String,
            userName: String,
            partnerId: String,
            partnerName: String,
            categoryId: String
        ): Flow<Resource<WasteTransaction>> = flow {
            emit(
                Resource.Success(
                    WasteTransaction(
                        id = "tx_test_123",
                        userId = userId,
                        userName = userName,
                        partnerId = partnerId,
                        partnerName = partnerName,
                        categoryId = categoryId,
                        quantityKg = 0.0,
                        status = EcoConstants.STATUS_PENDING,
                        pointsAwarded = 0
                    )
                )
            )
        }

        override fun getUserTransactions(userId: String): Flow<Resource<List<WasteTransaction>>> = flow { emit(Resource.Success(emptyList())) }
        override fun getPendingTransactionsForPartner(partnerId: String): Flow<Resource<List<WasteTransaction>>> = flow { emit(Resource.Success(emptyList())) }
        override fun confirmTransaction(transactionId: String, quantityKg: Double, collectorId: String): Flow<Resource<WasteTransaction>> = flow { emit(Resource.Success(WasteTransaction())) }
        override fun getWasteCategories(): Flow<Resource<List<WasteCategory>>> = flow { emit(Resource.Success(emptyList())) }
    }

    @Before
    fun setUp() {
        createPendingTransactionUseCase = CreatePendingTransactionUseCase(fakeRepository)
    }

    @Test
    fun `blank userId fails validation`() = runTest {
        val result = createPendingTransactionUseCase("", "Aarav", "partner_pari_01", "Pari Chowk Hub", "plastic").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("signed in")
    }

    @Test
    fun `blank partnerId fails validation`() = runTest {
        val result = createPendingTransactionUseCase("user_1", "Aarav", "", "Pari Chowk Hub", "plastic").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("valid drop-off station")
    }

    @Test
    fun `blank categoryId fails validation`() = runTest {
        val result = createPendingTransactionUseCase("user_1", "Aarav", "partner_pari_01", "Pari Chowk Hub", "").first()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("waste category")
    }

    @Test
    fun `valid parameters create pending transaction with 0 points awarded initially`() = runTest {
        val result = createPendingTransactionUseCase(
            userId = "user_1",
            userName = "Aarav Sharma",
            partnerId = "partner_pari_01",
            partnerName = "Pari Chowk Hub",
            categoryId = EcoConstants.WASTE_TYPE_E_WASTE
        ).first()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val tx = (result as Resource.Success).data
        assertThat(tx.status).isEqualTo(EcoConstants.STATUS_PENDING)
        assertThat(tx.isPending).isTrue()
        assertThat(tx.pointsAwarded).isEqualTo(0)
        assertThat(tx.quantityKg).isEqualTo(0.0)
    }
}
