package com.example.aplikasistoryapp

import androidx.test.espresso.IdlingResource
import com.example.aplikasistoryapp.ui.activity.LoginActivity

class GeneralIdlingResource(private val activity: LoginActivity) : IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName() = "GeneralIdlingResource"

    override fun isIdleNow(): Boolean {
        val isIdle = !activity.isLoading()
        if (isIdle) {
            callback?.onTransitionToIdle()
        }
        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }
}