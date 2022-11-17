package kr.evangers.rapidthemore.domain.usecase

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetADID @Inject constructor(
    @ApplicationContext private val context: Context
) : FlowUseCase<Unit, GetADID.Response> {
    override suspend fun invoke(request: Unit): Flow<Response> = flow {
        val adid = AdvertisingIdClient.getAdvertisingIdInfo(context).id
        if (adid != null) {
            emit(Response.Success(adid))
        } else {
            emit(Response.Failure(java.lang.RuntimeException()))
        }
    }

    sealed class Response {
        class Success(val adid: String) : Response()
        class Failure(val throwable: Throwable) : Response()
    }
}