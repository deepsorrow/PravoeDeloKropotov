package com.krop.pravoedelokropotov.ui.vm

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krop.pravoedelokropotov.domain.PravoeDeloClient
import com.krop.pravoedelokropotov.model.api.GetCodeResponse
import com.krop.pravoedelokropotov.ui.ProgressBarDialog
import com.krop.pravoedelokropotov.ui.vm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PhoneAuthScreenVm: BaseViewModel() {

    private var _phoneNumber = MutableStateFlow(defaultCountryCode)
    var phoneNumber: StateFlow<String> = _phoneNumber

    private var _goNextVisible = MutableStateFlow(false)
    var goNextVisible: StateFlow<Boolean> = _goNextVisible

    private val _goToOtpScreen = MutableSharedFlow<Bundle>()
    val goToOtpScreen: SharedFlow<Bundle> = _goToOtpScreen

    override fun showError(t: Throwable) {
        viewModelScope.launch {
            _progressBarVisibility.emit(false)
        }
        super.showError(t)
    }

    fun sendCode() {
        viewModelScope.launch {
            _progressBarVisibility.emit(true)
        }

        PravoeDeloClient.getInstance().getCode(phoneNumber.value.drop(1)).enqueue(object :
            Callback<GetCodeResponse> {
            override fun onResponse(
                call: Call<GetCodeResponse>,
                response: Response<GetCodeResponse>
            ) {
                val params = (response.body())?.run {
                     Bundle().apply {
                        putString(GetCodeResponse.CODE_KEY, code)
                        putString(GetCodeResponse.STATUS_KEY, status)
                    }
                } ?: showError(IllegalStateException("Ответ пустой!"))

                if (params is Bundle) {
                    viewModelScope.launch {
                        _goToOtpScreen.emit(params)
                        progressBarVisibility.emit(false)
                    }
                }
            }

            override fun onFailure(call: Call<GetCodeResponse>, t: Throwable) = showError(t)
        })
    }

    /**
     * Добавляет цифру цифру в [phoneNumber], если позволяет макс. длина номера [maxPhoneNumberLength].
     */
    fun inputDigit(number: Char) {
        phoneNumber.value.length >= maxPhoneNumberLength && return

        viewModelScope.launch {
            _phoneNumber.emit(phoneNumber.value + number)
            if (phoneNumber.value.length == maxPhoneNumberLength) {
                _goNextVisible.emit(true)
            }
        }
    }

    /**
     * Удаляет последнюю цифру из [phoneNumber], если он не равен [defaultCountryCode].
     */
    fun backspaceDigit() {
        phoneNumber.value == defaultCountryCode && return

        viewModelScope.launch {
            _phoneNumber.emit(phoneNumber.value.dropLast(1))
            _goNextVisible.emit(false)
        }
    }

    fun formatNumber(): String =
        if (phoneNumber.value.length == maxPhoneNumberLength) {
            val regex = """(\d)(\d{3})(\d{3})(\d{2})(\d{2})""".toRegex()
            regex.replace(phoneNumber.value, "$1 $2 $3 $4 $5")
        } else {
            phoneNumber.value
        }


    companion object {
        private const val defaultCountryCode = "+7"
        private const val maxPhoneNumberLength = 12
    }
}