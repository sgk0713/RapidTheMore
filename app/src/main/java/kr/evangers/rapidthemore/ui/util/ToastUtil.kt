package kr.evangers.rapidthemore.ui.util

import android.content.Context
import android.widget.Toast

var toast: Toast? = null

fun Context.shortToast(msg: String) {
    toast?.cancel()
    toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    toast?.show()
}

fun Context.longToast(msg: String) {
    toast?.cancel()
    toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
    toast?.show()
}