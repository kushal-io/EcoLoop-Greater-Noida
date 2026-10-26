package com.ecoloop.greaternoida.di

import android.content.Context
import com.ecoloop.greaternoida.data.repository.AuthRepositoryImpl
import com.ecoloop.greaternoida.data.repository.PartnerRepositoryImpl
import com.ecoloop.greaternoida.data.repository.RewardRepositoryImpl
import com.ecoloop.greaternoida.data.repository.TransactionRepositoryImpl
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import com.ecoloop.greaternoida.data.ml.TfLiteWasteClassifier
import com.ecoloop.greaternoida.domain.ml.ImageClassifier
import com.ecoloop.greaternoida.domain.usecase.CalculatePointsUseCase
import com.ecoloop.greaternoida.domain.usecase.ClassifyWasteImageUseCase
import com.ecoloop.greaternoida.domain.usecase.GetRecommendedCentresUseCase
import com.ecoloop.greaternoida.domain.usecase.ConfirmTransactionUseCase
import com.ecoloop.greaternoida.domain.usecase.CreatePendingTransactionUseCase
import com.ecoloop.greaternoida.domain.usecase.GetCurrentUserUseCase
import com.ecoloop.greaternoida.domain.usecase.GetPendingTransactionsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetRewardsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetUserRedemptionsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetUserTransactionsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetWasteCategoriesUseCase
import com.ecoloop.greaternoida.domain.usecase.RedeemRewardUseCase
import com.ecoloop.greaternoida.domain.usecase.SignInUseCase
import com.ecoloop.greaternoida.domain.usecase.SignOutUseCase
import com.ecoloop.greaternoida.domain.usecase.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun providePartnerRepository(
        firestore: FirebaseFirestore
    ): PartnerRepository {
        return PartnerRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore, context)
    }

    @Provides
    @Singleton
    fun provideCalculatePointsUseCase(): CalculatePointsUseCase {
        return CalculatePointsUseCase()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        calculatePointsUseCase: CalculatePointsUseCase,
        @ApplicationContext context: Context
    ): TransactionRepository {
        return TransactionRepositoryImpl(firestore, calculatePointsUseCase, context)
    }

    @Provides
    @Singleton
    fun provideCreatePendingTransactionUseCase(
        transactionRepository: TransactionRepository
    ): CreatePendingTransactionUseCase {
        return CreatePendingTransactionUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideConfirmTransactionUseCase(
        transactionRepository: TransactionRepository
    ): ConfirmTransactionUseCase {
        return ConfirmTransactionUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserTransactionsUseCase(
        transactionRepository: TransactionRepository
    ): GetUserTransactionsUseCase {
        return GetUserTransactionsUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetPendingTransactionsUseCase(
        transactionRepository: TransactionRepository
    ): GetPendingTransactionsUseCase {
        return GetPendingTransactionsUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetWasteCategoriesUseCase(
        transactionRepository: TransactionRepository
    ): GetWasteCategoriesUseCase {
        return GetWasteCategoriesUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase {
        return SignInUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: AuthRepository): SignOutUseCase {
        return SignOutUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(authRepository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideRewardRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        @ApplicationContext context: Context
    ): RewardRepository {
        return RewardRepositoryImpl(firestore, authRepository, context)
    }

    @Provides
    @Singleton
    fun provideGetRewardsUseCase(rewardRepository: RewardRepository): GetRewardsUseCase {
        return GetRewardsUseCase(rewardRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserRedemptionsUseCase(rewardRepository: RewardRepository): GetUserRedemptionsUseCase {
        return GetUserRedemptionsUseCase(rewardRepository)
    }

    @Provides
    @Singleton
    fun provideRedeemRewardUseCase(rewardRepository: RewardRepository): RedeemRewardUseCase {
        return RedeemRewardUseCase(rewardRepository)
    }

    @Provides
    @Singleton
    fun provideImageClassifier(
        @ApplicationContext context: Context
    ): ImageClassifier {
        return TfLiteWasteClassifier(context)
    }

    @Provides
    @Singleton
    fun provideClassifyWasteImageUseCase(imageClassifier: ImageClassifier): ClassifyWasteImageUseCase {
        return ClassifyWasteImageUseCase(imageClassifier)
    }

    @Provides
    @Singleton
    fun provideGetRecommendedCentresUseCase(partnerRepository: PartnerRepository): GetRecommendedCentresUseCase {
        return GetRecommendedCentresUseCase(partnerRepository)
    }
}
