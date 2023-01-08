package kr.evangers.rapidthemore.ui.main

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.evangers.rapidthemore.domain.usecase.*
import kr.evangers.rapidthemore.ui.firebase.*
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseEventLogger: FirebaseEventLogger,
    private val getDigitAddedFinalValue: GetDigitAddedFinalValue,
    private val removeLastDigit: RemoveLastDigit,
    private val getTheMoreDiscountRatio: GetTheMoreDiscountRatio,
    private val clearAmount: ClearAmount,
    private val getADID: GetADID,
) : ViewModel() {

    private var inputEventLoggerJob: Job? = null
    private val inputTimer = flow {
        delay(1000L)
        emit(Unit)
    }
    private val state = HomeState()
    val liveData = MutableLiveData<IHomeState>(state)


    fun start() {
        clearNumber()
        updateADID()
    }

    private fun updateADID() {
        viewModelScope.launch {
            getADID.invoke(Unit)
                .flowOn(Dispatchers.IO)
                .catch {
                    // do nothing
                }
                .collect {
                    when (it) {
                        is GetADID.Response.Failure -> {
                        }
                        is GetADID.Response.Success -> {
                            state.update(HomeAction.UpdateAdid(it.adid))
                            liveData.postValue(state)
                        }
                    }
                }
        }
    }

    fun addDigitLast(digit: Int) {
        if (state.isDigitAddable.not()) {
            state.update(HomeAction.ShowToast("12자리 이상 추가할 수 없습니다."))
            liveData.postValue(state)
            return
        }
        viewModelScope.launch {
            getDigitAddedFinalValue(GetDigitAddedFinalValue.Request(digit))
                .map { result ->
                    when (result) {
                        is GetDigitAddedFinalValue.Response.Success -> {
                            calculateRatio(result.amount)
                            HomeAction.UpdateAmount(result.amount)
                        }
                        is GetDigitAddedFinalValue.Response.Failure -> {
                            HomeAction.UpdateAmount(BigDecimal.ZERO)
                        }
                    }
                }
                .collect { result ->
                    state.update(result)
                    liveData.postValue(state)
                    sendInputPriceLog()
                }
        }
    }

    fun removeDigitLast() {
        viewModelScope.launch {
            removeLastDigit(Unit)
                .map { result ->
                    when (result) {
                        is RemoveLastDigit.Response.Success -> {
                            calculateRatio(result.amount)
                            HomeAction.UpdateAmount(result.amount)
                        }
                        is RemoveLastDigit.Response.Failure -> {
                            HomeAction.UpdateAmount(BigDecimal.ZERO)
                        }
                    }
                }
                .collect { result ->
                    state.update(result)
                    liveData.postValue(state)
                    Log.d("!@#!@#", "숫자 제거")
                    sendInputPriceLog()
                }
        }
    }

    private suspend fun calculateRatio(amount: BigDecimal) {
        getTheMoreDiscountRatio(GetTheMoreDiscountRatio.Request(amount))
            .map {
                when (it) {
                    is GetTheMoreDiscountRatio.Response.Failure -> {
                        throw it.t
                    }
                    is GetTheMoreDiscountRatio.Response.Success -> {
                        HomeAction.UpdateRatio(it.ratio)
                    }
                }
            }
            .collect {
                state.update(it)
                liveData.postValue(state)
            }
    }

    fun clearNumber() {
        viewModelScope.launch {
            sendInputPriceLog()
            clearAmount(Unit)
                .map {
                    when (it) {
                        is ClearAmount.Response.Failure -> {
                            HomeAction.Error(it.t)
                        }
                        ClearAmount.Response.Success -> {
                            HomeAction.ClearDigits
                        }
                    }

                }
                .collect {
                    state.update(it)
                    liveData.postValue(state)
                }
        }
    }

    fun showPercentToast() {
        val ratioText = "${state.ratio.getValue() ?: 0.0}%"
        state.update(HomeAction.ShowLongToast(ratioText))
        liveData.postValue(state)
    }

    fun showToast(msg: String) {
        state.update(HomeAction.ShowToast(msg))
        liveData.postValue(state)
    }

    fun launchPayco() {
        firebaseEventLogger.logEvent(click_launchpay, mapOf(
            param_app to payco
        ))
        state.update(HomeAction.LaunchPayco)
        liveData.postValue(state)
    }

    fun launchSpay() {
        firebaseEventLogger.logEvent(click_launchpay, mapOf(
            param_app to samsungpay
        ))
        state.update(HomeAction.LaunchSpay)
        liveData.postValue(state)
    }

    fun launchKbpay() {
        firebaseEventLogger.logEvent(click_launchpay, mapOf(
            param_app to kbpay
        ))
        state.update(HomeAction.LaunchKbpay)
        liveData.postValue(state)
    }

    fun navToIntent(intent: Intent) {
        state.update(HomeAction.NavToIntent(intent))
        liveData.postValue(state)
    }

    private fun sendInputPriceLog() {
        inputEventLoggerJob?.cancel()
        val amount: Long = state.amount.getValue()?.toLong() ?: 0L
        val ratio = state.ratio.getValue() ?: 0L
        inputEventLoggerJob = CoroutineScope(Dispatchers.IO).launch {
            inputTimer.collect {
                if (amount > 5000) {
                    firebaseEventLogger.logEvent(input_price, mapOf(
                        param_price to amount,
                        param_ratio to ratio
                    ))
                }
            }
        }
    }
}