package kr.evangers.rapidthemore.ui.main

import android.content.Intent
import java.math.BigDecimal

sealed class HomeAction {
    object ClearDigits : HomeAction()
    object LaunchNaverPay : HomeAction()
    object LaunchSpay : HomeAction()
    object LaunchKbpay : HomeAction()
    class NavToIntent(val intent: Intent) : HomeAction()
    class UpdateAdid(val adid: String) : HomeAction()

    class Error(t: Throwable) : HomeAction()

    class UpdateRatio(val ratio: Float) : HomeAction()
    class UpdateAmount(val number: BigDecimal) : HomeAction()
    class ShowToast(val msg: String) : HomeAction()
    class ShowLongToast(val msg: String) : HomeAction()
}