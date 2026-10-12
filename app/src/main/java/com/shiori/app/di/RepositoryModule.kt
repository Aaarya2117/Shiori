package com.shiori.app.di

import com.shiori.app.data.repository.GameRepositoryImpl
import com.shiori.app.data.repository.PatientProfileRepositoryImpl
import com.shiori.app.data.repository.ReminderRepositoryImpl
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IPatientRepository
import com.shiori.app.domain.repository.IReminderRepository
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
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): IReminderRepository

    @Binds
    @Singleton
    abstract fun bindPatientProfileRepository(impl: PatientProfileRepositoryImpl): IPatientRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): IGameRepository
}
