package com.example.authentication.utils

import com.example.authentication.data.UniqueEmailValidateResponse
import com.example.authentication.data.ValidateEmailBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiConsumer {

    @POST("user/validate-unique-email")
    suspend fun validateEmailAddress(@Body body: ValidateEmailBody) : Response<UniqueEmailValidateResponse>
}