package kr.evangers.rapidthemore.data.di

import kr.evangers.rapidthemore.data.repository.CalculatorRepository
import kr.evangers.rapidthemore.data.repository.TheMoreRepository
import kr.evangers.rapidthemore.domain.repository.ICalculatorRepository
import kr.evangers.rapidthemore.domain.repository.ITheMoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindCalculatorRepository(
        calculatorRepository: CalculatorRepository
    ): ICalculatorRepository

    @Singleton
    @Binds
    abstract fun bindTheMoreRepository(
        theMoreRepository: TheMoreRepository
    ): ITheMoreRepository
}