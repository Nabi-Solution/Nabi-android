package com.gdghufs.nabi.di // 실제 패키지 경로

import com.gdghufs.nabi.data.repository.ChatAppointmentRepository
import com.gdghufs.nabi.data.repository.ChatAppointmentRepositoryImpl
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.data.repository.UserRepositoryImpl
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
        authRepositoryImpl: UserRepositoryImpl
    ): UserRepository

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

    @Binds
    @Singleton
    abstract fun bindChatAppointmentRepository(
        chatAppointmentRepositoryImpl: ChatAppointmentRepositoryImpl
    ): ChatAppointmentRepository
}