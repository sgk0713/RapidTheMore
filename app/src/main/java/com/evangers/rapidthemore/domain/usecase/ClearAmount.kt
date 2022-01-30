package com.evangers.rapidthemore.domain.usecase

import com.evangers.rapidthemore.domain.repository.ICalculatorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ClearAmount @Inject constructor(
    private val calculatorRepository: ICalculatorRepository
) : FlowUseCase<Unit, ClearAmount.Response> {
    sealed class Response {
        object Success : Response()
        class Failure(val t: Throwable) : Response()
    }

    override suspend fun invoke(request: Unit): Flow<Response> = flow {
        try {
            calculatorRepository.clearAmount()
            emit(Response.Success)
        } catch (e: Exception) {
            emit(Response.Failure(e))
        }

    }
}