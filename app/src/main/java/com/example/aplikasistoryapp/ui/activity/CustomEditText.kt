package com.example.aplikasistoryapp.ui.activity

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.aplikasistoryapp.R

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when (id) {
                    R.id.ed_register_password, R.id.ed_login_password -> validatePassword()
                    R.id.ed_register_email, R.id.ed_login_email -> validateEmail()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateEmail() {
        val email = text.toString()
        error = if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            context.getString(R.string.email_invalid_error)
        } else {
            null
        }
    }

    private fun validatePassword() {
        val password = text.toString()
        error = if (password.length < 8) {
            context.getString(R.string.password_length_error)
        } else {
            null
        }
    }
}