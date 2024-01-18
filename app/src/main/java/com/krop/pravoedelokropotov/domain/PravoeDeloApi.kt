package com.krop.pravoedelokropotov.domain

import com.krop.pravoedelokropotov.model.api.GetCodeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PravoeDeloApi {
    @GET("getCode")
    fun getCode(@Query("login") login: String): Call<GetCodeResponse>

    @GET("getToken")
    fun getToken(@Query("login") login: String, @Query("password") password: String): Call<Any>

    @GET("regenerateCode")
    fun regenerateCode(@Query("login") login: String): Call<Any>
}