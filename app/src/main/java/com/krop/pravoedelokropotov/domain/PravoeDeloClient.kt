package com.krop.pravoedelokropotov.domain

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PravoeDeloClient {
    private var instance: PravoeDeloApi? = null

    fun getInstance(): PravoeDeloApi {
        if (instance == null) {
            val gson = GsonBuilder().setLenient().create()

            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(PravoeDeloApi::class.java)
        }
        return instance!!
    }

    private const val BASE_URL = "https://lk.pravoe-delo.su/api/v1/"
}