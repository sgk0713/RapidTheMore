package com.evangers.rapidthemore.domain.usecase

import kotlinx.coroutines.flow.Flow


interface FlowUseCase<in T, out R> {
    suspend operator fun invoke(request: T): Flow<R>
}