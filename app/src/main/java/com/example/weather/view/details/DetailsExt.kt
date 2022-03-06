package com.example.weather.view.details

import android.view.View
import com.google.android.material.snackbar.Snackbar

// extension-функция для Snackbar (при ошибке приложения)
fun View.showSnackBarDetail(
    text: String,
    actionText: String,
    action: (View) -> Unit,
    length: Int = Snackbar.LENGTH_SHORT
) {
    Snackbar.make(this, text, length).setAction(actionText, action).show()
}