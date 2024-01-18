package com.krop.pravoedelokropotov.model.api

data class GetCodeResponse(
    val code: String,
    val status: String,
    val error: String
) {

    companion object {
        const val CODE_KEY = "code"
        const val STATUS_KEY = "status"
    }
}