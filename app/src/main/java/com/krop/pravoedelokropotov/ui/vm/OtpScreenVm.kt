package com.krop.pravoedelokropotov.ui.vm

import android.os.CountDownTimer
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.krop.pravoedelokropotov.domain.PravoeDeloClient
import com.krop.pravoedelokropotov.model.api.ErrorResponse
import com.krop.pravoedelokropotov.ui.vm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpScreenVm: BaseViewModel() {

    var phoneNumber: String = ""

    private val _showResendButton = MutableSharedFlow<Unit>()
    val showResendButton: SharedFlow<Unit> = _showResendButton

    private val _goToWelcomeScreenWithToken = MutableSharedFlow<String>()
    val goToWelcomeScreenWithToken: SharedFlow<String> = _goToWelcomeScreenWithToken

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    private var timer: CountDownTimer = object : CountDownTimer(COUNTDOWN_MS, COUNTDOWN_TICK_MS) {
        override fun onTick(millisUntilFinished: Long) {
            updateTimer(millisUntilFinished / 1000)
        }

        override fun onFinish() {
            viewModelScope.launch {
                _showResendButton.emit(Unit)
            }
        }

    }

    var actualCode: String? = null

    fun resendCode() {
        PravoeDeloClient.getInstance().regenerateCode(phoneNumber).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                val jsonString = response.body().toString()
                viewModelScope.launch {
                    if (jsonString.contains("error")) {
                        val error = Gson().fromJson(jsonString, ErrorResponse::class.java)
                        _showError.emit(error.error)
                    } else {
                        actualCode = jsonString
                        _showError.emit("Код был выслан повторно")
                    }

                    timer.start()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) = showError(t)
        })
    }

    fun validateCode(enteredCode: String) {
        if (enteredCode == actualCode) {
            viewModelScope.launch {
                _progressBarVisibility.emit(true)
            }

            PravoeDeloClient.getInstance().getToken(phoneNumber, enteredCode).enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    viewModelScope.launch {
                        _goToWelcomeScreenWithToken.emit(response.body().toString())
                        _progressBarVisibility.emit(false)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) = showError(t)
            })
        } else {
            viewModelScope.launch {
                _showResendButton.emit(Unit)
                _showError.emit("Неверный код!")
            }
        }
    }

    fun updateTimer(secondsLeft: Long) {
        viewModelScope.launch {
            _resendTimer.emit(secondsLeft.toInt())
        }
    }

    private companion object {
        private const val COUNTDOWN_MS = 5000L
        private const val COUNTDOWN_TICK_MS = 1000L
    }
}