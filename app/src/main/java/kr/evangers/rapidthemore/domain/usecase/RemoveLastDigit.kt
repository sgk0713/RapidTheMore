package kr.evangers.rapidthemore.domain.usecase

import kr.evangers.rapidthemore.domain.repository.ICalculatorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

class RemoveLastDigit @Inject constructor(
    private val calculatorRepository: ICalculatorRepository
) : FlowUseCase<Unit, RemoveLastDigit.Response> {

    sealed class Response {
        class Success(val amount: BigDecimal) : Response()
        class Failure(val throwable: Throwable) : Response()
    }

    override suspend fun invoke(request: Unit): Flow<Response> = flow {
        try {
            val value = withContext(Dispatchers.Default) {
                calculatorRepository.removeLastDigit()
                calculatorRepository.getAmount()
            }
            emit(Response.Success(value))
        } catch (e: Exception) {
            emit(Response.Failure(e))
        }
    }

}