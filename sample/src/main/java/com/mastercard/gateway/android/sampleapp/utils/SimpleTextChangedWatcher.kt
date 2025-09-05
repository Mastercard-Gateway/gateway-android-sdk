package com.mastercard.gateway.android.sampleapp.utils


import android.text.Editable
import android.text.TextWatcher

class SimpleTextChangedWatcher(private val onAnyChange: Runnable) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onAnyChange.run()
    }
    override fun afterTextChanged(s: Editable?) {}
}
