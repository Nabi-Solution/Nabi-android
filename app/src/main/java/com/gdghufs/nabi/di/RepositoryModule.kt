package com.gdghufs.nabi.di // 실제 패키지 경로

import android.service.autofill.UserData
import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.data.datasource.FirebaseAuthDataSource
import com.gdghufs.nabi.data.repository.AuthRepository
import com.gdghufs.nabi.data.repository.AuthRepositoryImpl
import com.gdghufs.nabi.data.repository.ChatRepository
import com.gdghufs.nabi.data.repository.ChatRepositoryImpl
import com.gdghufs.nabi.data.repository.TextToSpeechRepository
import com.gdghufs.nabi.data.repository.TextToSpeechRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindTextToSpeechRepository(
        textToSpeechRepositoryImpl: TextToSpeechRepositoryImpl
    ): TextToSpeechRepository
}