package com.evangers.rapidthemore.domain.usecase

interface SuspendUseCase<in T, out R> {
    suspend operator fun invoke(request: T): R
}