package com.krop.pravoedelokropotov.ui.vm.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    protected val _showError = MutableSharedFlow<String>()
    val showError: SharedFlow<String> = _showError

    protected val _progressBarVisibility = MutableStateFlow(false)
    val progressBarVisibility = _progressBarVisibility

    protected open fun showError(t: Throwable) {
        viewModelScope.launch {
            _showError.emit(t.localizedMessage.orEmpty())
            _progressBarVisibility.emit(true)
        }
    }
}