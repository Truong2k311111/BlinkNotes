package com.example.blinknotes.data.remote

import com.example.blinknotes.data.model.SendMessageDto
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("send")
    suspend fun sendMessage(@Body message: SendMessageDto): Response<Unit>
}

object FcmApiProvider {
    fun create(): FcmApi {
        return Retrofit.Builder()
            .baseUrl("https://blinknotes-api.onrender.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }
}