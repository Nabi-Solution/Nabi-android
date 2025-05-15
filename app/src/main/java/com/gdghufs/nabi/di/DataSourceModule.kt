package com.gdghufs.nabi.di

import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.data.datasource.FirebaseAuthDataSource
import com.gdghufs.nabi.data.datasource.FirestoreUserDataSourceImpl
import com.gdghufs.nabi.data.datasource.UserDataSource
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
}