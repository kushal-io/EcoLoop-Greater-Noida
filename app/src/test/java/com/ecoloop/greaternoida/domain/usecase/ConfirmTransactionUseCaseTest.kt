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

class ConfirmTransactionUseCaseTest {

    private lateinit var confirmTransactionUseCase: ConfirmTransactionUseCase

    private val fakeRepository = object : TransactionRepository {
        var callCount = 0
        var lastConfirmedTx: WasteTransaction? = null

        override fun createPendingTransaction(
            userId: String,
            userName: String,
            partnerId: String,
            partnerName: String,
            categoryId: String
        ): Flow<Resource<WasteTransaction>> = flow {
            emit(Resource.Success(WasteTransaction()))
        }

        override fun getUserTransactions(userId: String): Flow<Resource<List<WasteTransaction>>> = flow {
            emit(Resource.Success(emptyList()))
        }

        override fun getPendingTransactionsForPartner(partnerId: String): Flow<Resource<List<WasteTransaction>>> = flow {
            emit(Resource.Success(emptyList()))
        }

        override fun confirmTransaction(
            transactionId: String,
            quantityKg: Double,
            collectorId: String
        ): Flow<Resource<WasteTransaction>> = flow {
            callCount++
            // Double-crediting check simulation: if already confirmed, reject
            if (lastConfirmedTx?.id == transactionId && lastConfirmedTx?.isConfirmed == true) {
                emit(Resource.Error("Transaction already confirmed. Double-crediting prevented."))
                return@flow
            }

            val confirmed = WasteTransaction(
                id = transactionId,
                userId = "user_123",
                userName = "Aarav",
                partnerId = "partner_pari_01",
                categoryId = EcoConstants.WASTE_TYPE_E_WASTE,
                quantityKg = quantityKg,
                status = EcoConstants.STATUS_CONFIRMED,
                pointsAwarded = (quantityKg * 20 * 2.5).toInt(),
                confirmedAt = System.currentTimeMillis(),
                collectorId = collectorId
            )
            lastConfirmedTx = confirmed
            emit(Resource.Success(confirmed))
        }

        override fun getWasteCategories(): Flow<Resource<List<WasteCategory>>> = flow {
            emit(Resource.Success(emptyList()))
        }
    }

    @Before
    fun setUp() {
        confirmTransactionUseCase = ConfirmTransactionUseCase(fakeRepository)
    }

    @Test
    fun `zero or negative quantity fails validation`() = runTest {
        val resultZero = confirmTransactionUseCase("tx_1", 0.0, "collector_1").first()
        val resultNeg = confirmTransactionUseCase("tx_1", -2.5, "collector_1").first()

        assertThat(resultZero).isInstanceOf(Resource.Error::class.java)
        assertThat(resultNeg).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `blank transaction id or collector id fails validation`() = runTest {
        val resultNoTx = confirmTransactionUseCase("", 5.0, "collector_1").first()
        val resultNoCol = confirmTransactionUseCase("tx_1", 5.0, "").first()

        assertThat(resultNoTx).isInstanceOf(Resource.Error::class.java)
        assertThat(resultNoCol).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `valid confirmation awards calculated points and confirms status`() = runTest {
        val result = confirmTransactionUseCase("tx_1", 3.0, "collector_pari").first()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val tx = (result as Resource.Success).data
        assertThat(tx.status).isEqualTo(EcoConstants.STATUS_CONFIRMED)
        assertThat(tx.isConfirmed).isTrue()
        assertThat(tx.quantityKg).isEqualTo(3.0)
        // E-waste: 3.0 kg * 20 * 2.5 = 150 points
        assertThat(tx.pointsAwarded).isEqualTo(150)
    }

    @Test
    fun `attempting to confirm an already confirmed transaction prevents double crediting`() = runTest {
        // First confirmation succeeds
        val firstResult = confirmTransactionUseCase("tx_double_check", 2.0, "collector_1").first()
        assertThat(firstResult).isInstanceOf(Resource.Success::class.java)

        // Second confirmation with same tx ID is rejected
        val secondResult = confirmTransactionUseCase("tx_double_check", 2.0, "collector_1").first()
        assertThat(secondResult).isInstanceOf(Resource.Error::class.java)
        assertThat((secondResult as Resource.Error).message).contains("Double-crediting prevented")
    }
}
