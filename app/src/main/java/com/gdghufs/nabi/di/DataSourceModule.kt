package com.gdghufs.nabi.di

import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.data.datasource.ChatAppointmentDataSource
import com.gdghufs.nabi.data.datasource.ChatDataSource
import com.gdghufs.nabi.data.datasource.FirebaseAuthDataSource
import com.gdghufs.nabi.data.datasource.FirebaseChatDataSource
import com.gdghufs.nabi.data.datasource.FirestoreChatAppointmentDataSource
import com.gdghufs.nabi.data.datasource.FirestoreUserDataSourceImpl
import com.gdghufs.nabi.data.datasource.HabitDataSource
import com.gdghufs.nabi.data.datasource.MedicationDataSource
import com.gdghufs.nabi.data.datasource.TextToSpeechDataSource
import com.gdghufs.nabi.data.datasource.TextToSpeechRemoteDataSource
import com.gdghufs.nabi.data.datasource.UserDataSource
import com.gdghufs.nabi.data.source.HabitDataSourceImpl
import com.gdghufs.nabi.data.source.MedicationDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAuthDataSource(
        firebaseAuthDataSource: FirebaseAuthDataSource
    ): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindUserDataSource(
        firebaseUserDataSourceImpl: FirestoreUserDataSourceImpl
    ): UserDataSource

    @Binds
    @Singleton
    abstract fun bindChatDataSource(
        firebaseChatDataSource: FirebaseChatDataSource
    ): ChatDataSource

    @Binds
    @Singleton
    abstract fun bindTextToSpeechDataSource(
        textToSpeechRemoteDataSource: TextToSpeechRemoteDataSource
    ): TextToSpeechDataSource

    @Binds
    @Singleton
    abstract fun bindChatAppointmentDataSource(
        firebaseChatAppointmentDataSource: FirestoreChatAppointmentDataSource
    ): ChatAppointmentDataSource

    @Binds
    @Singleton
    abstract fun bindHabitDataSource(impl: HabitDataSourceImpl): HabitDataSource

    @Binds
    @Singleton
    abstract fun bindMedicationDataSource(impl: MedicationDataSourceImpl): MedicationDataSource
}