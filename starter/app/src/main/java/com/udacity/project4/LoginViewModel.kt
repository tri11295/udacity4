package com.udacity.project4

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource

class LoginViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}