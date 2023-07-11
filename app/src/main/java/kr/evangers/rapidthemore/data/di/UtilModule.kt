package kr.evangers.rapidthemore.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.evangers.rapidthemore.ui.util.AppOpenAdManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UtilModule {

    @Singleton
    @Provides
    fun provideAppOpenAdManger() = AppOpenAdManager()

}