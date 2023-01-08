package kr.evangers.rapidthemore.ui.base.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.evangers.rapidthemore.ui.firebase.EventLogger
import kr.evangers.rapidthemore.ui.firebase.FirebaseEventLogger
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class FirebaseModule {
    @Singleton
    @Binds
    abstract fun bindFirebaseAnalytics(
        firebaseEventLogger: FirebaseEventLogger,
    ): EventLogger
}
