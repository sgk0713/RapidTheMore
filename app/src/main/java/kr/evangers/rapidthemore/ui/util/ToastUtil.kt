package kr.evangers.rapidthemore.ui.util

import android.content.Context
import android.widget.Toast

var toast: Toast? = null

fun Context.shortToast(msg: String) {
    if (toast == null) toast = Toast(this)
    toast?.run {
        cancel()
        setText(msg)
        duration = Toast.LENGTH_SHORT
        show()
    }
}

fun Context.longToast(msg: String) {
    if (toast == null) toast = Toast(this)
    toast?.run {
        cancel()
        setText(msg)
        duration = Toast.LENGTH_LONG
        show()
    }
}