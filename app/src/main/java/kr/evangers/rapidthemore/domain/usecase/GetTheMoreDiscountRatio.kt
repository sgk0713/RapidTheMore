package kr.evangers.rapidthemore.domain.usecase

import kr.evangers.rapidthemore.domain.repository.ITheMoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

class GetTheMoreDiscountRatio @Inject constructor(
    private val theMoreRepository: ITheMoreRepository
) : FlowUseCase<GetTheMoreDiscountRatio.Request, GetTheMoreDiscountRatio.Response> {
    class Request(val amount: BigDecimal)
    sealed class Response {
        class Success(val ratio: Float) : Response()
        class Failure(val t: Throwable) : Response()
    }

    override suspend fun invoke(request: Request): Flow<Response> = flow {
        try {
            val ratio = withContext(Dispatchers.Default) {
                theMoreRepository.getRatio(request.amount)
            }
            emit(Response.Success(ratio))
        } catch (e: Exception) {
            error(Response.Failure(e))
        }

    }
}