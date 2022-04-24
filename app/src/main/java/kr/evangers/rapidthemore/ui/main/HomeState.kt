package kr.evangers.rapidthemore.ui.main

import android.content.Intent
import kr.evangers.rapidthemore.ui.base.Event
import java.math.BigDecimal

interface IHomeState {
    var ratio: Event<Float>
    var amount: Event<BigDecimal>
    var toastMessage: Event<String>?
    var longToastMessage: Event<String>?
    var launchPayco: Event<Unit>?
    var launchSpay: Event<Unit>?
    var intent: Event<Intent>?
}

class HomeState constructor(
    override var ratio: Event<Float> = Event(0f),
    override var amount: Event<BigDecimal> = Event(BigDecimal(0)),
    override var toastMessage: Event<String>? = null,
    override var longToastMessage: Event<String>? = null,
    override var launchPayco: Event<Unit>? = null,
    override var launchSpay: Event<Unit>? = null,
    override var intent: Event<Intent>? = null
) : IHomeState {

    val isDigitAddable: Boolean
        get() {
            return amount.getValue()?.toString()?.length ?: 0 < 12
        }

    fun update(action: HomeAction) {
        when (action) {
            is HomeAction.UpdateRatio -> {
                ratio = Event(action.ratio)
            }
            is HomeAction.UpdateAmount -> {
                amount = Event(action.number)
            }
            is HomeAction.ClearDigits -> {
                ratio = Event(0.0f)
                amount = Event(BigDecimal.ZERO)
            }
            is HomeAction.ShowToast -> {
                toastMessage = Event(action.msg)
            }
            is HomeAction.ShowLongToast -> {
                longToastMessage = Event(action.msg)
            }
            HomeAction.LaunchPayco -> {
                launchPayco = Event(Unit)
            }
            HomeAction.LaunchSpay -> {
                launchSpay = Event(Unit)
            }
            is HomeAction.NavToIntent -> {
                intent = Event(action.intent)
            }
            is HomeAction.Error -> Unit
        }
    }
}