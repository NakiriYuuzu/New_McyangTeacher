package tw.edu.teachermcyang.yuuzu_lib

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import tw.edu.teachermcyang.AppConfig

class ViewHelper(
    private val activity: Activity
) {
    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(view: View) {
        try {
            if (view !is TextInputEditText) {
                view.setOnTouchListener { _: View?, _: MotionEvent? ->
                    hideSoftKeyboard()
                    false
                }
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val innerView = view.getChildAt(i)
                    setupUI(innerView)
                }
            }
        } catch (e: Exception) {
            Log.e(AppConfig.TAG, "setupUI: $e")
        }
    }

    private fun hideSoftKeyboard() {
        try {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        } catch (e: Exception) {
            Log.e(AppConfig.TAG, "hideSoftKeyboard: $e")
        }
    }
}