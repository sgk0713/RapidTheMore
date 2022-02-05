package com.evangers.rapidthemore.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evangers.rapidthemore.domain.usecase.ClearAmount
import com.evangers.rapidthemore.domain.usecase.GetDigitAddedFinalValue
import com.evangers.rapidthemore.domain.usecase.GetTheMoreDiscountRatio
import com.evangers.rapidthemore.domain.usecase.RemoveLastDigit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDigitAddedFinalValue: GetDigitAddedFinalValue,
    private val removeLastDigit: RemoveLastDigit,
    private val getTheMoreDiscountRatio: GetTheMoreDiscountRatio,
    private val clearAmount: ClearAmount
) : ViewModel() {

    private val state = HomeState()
    val liveData = MutableLiveData<IHomeState>(state)


    fun start() {
        clearNumber()
    }

    fun addDigitLast(digit: Int) {
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
                }
        }
    }

    private fun calculateRatio(amount: BigDecimal) {
        viewModelScope.launch {
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
    }

    fun clearNumber() {
        viewModelScope.launch {
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
        state.update(HomeAction.LaunchPayco)
        liveData.postValue(state)
    }

    fun launchSpay() {
        state.update(HomeAction.LaunchSpay)
        liveData.postValue(state)
    }
}