package org.sdelaysam.util.view

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created on 7/9/20.
 * @author sdelaysam
 */

fun View.hideKeyboard() {
    if (hasFocus()) {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.hideSoftInputFromWindow(windowToken, 0)
    }
}