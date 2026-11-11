package com.shiori.app.di

import com.shiori.app.ml.DifficultyAdapterModel
import com.shiori.app.ml.Tier1StaircaseDifficultyAdapter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {
    @Binds
    @Singleton
    abstract fun bindDifficultyAdapter(impl: Tier1StaircaseDifficultyAdapter): DifficultyAdapterModel
}
