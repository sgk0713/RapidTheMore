package kr.evangers.rapidthemore.domain.usecase

import kr.evangers.rapidthemore.domain.repository.ICalculatorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

class GetDigitAddedFinalValue @Inject constructor(
    private val calculatorRepository: ICalculatorRepository
) : FlowUseCase<GetDigitAddedFinalValue.Request, GetDigitAddedFinalValue.Response> {

    class Request(
        val addValue: Int
    )

    sealed class Response {
        class Success(val amount: BigDecimal) : Response()
        class Failure(val throwable: Throwable) : Response()
    }

    override suspend fun invoke(request: Request): Flow<Response> = flow {
        try {
            val value = withContext(Dispatchers.Default) {
                calculatorRepository.addUnitDigitFrom(request.addValue)
                calculatorRepository.getAmount()
            }
            emit(Response.Success(value))
        } catch (e: Exception) {
            emit(Response.Failure(e))
        }
    }
}