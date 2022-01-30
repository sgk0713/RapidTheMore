package com.evangers.rapidthemore.ui.main

import java.math.BigDecimal

sealed class HomeAction {
    object ClearDigits : HomeAction()
    object LaunchPayco : HomeAction()
    object LaunchSpay : HomeAction()

    class Error(t: Throwable) : HomeAction()

    class UpdateRatio(val ratio: Float) : HomeAction()
    class UpdateAmount(val number: BigDecimal) : HomeAction()
    class ShowToast(val msg: String) : HomeAction()
}