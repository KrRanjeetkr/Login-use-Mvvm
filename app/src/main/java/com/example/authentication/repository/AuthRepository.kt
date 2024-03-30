package com.example.authentication.repository

import com.example.authentication.data.ValidateEmailBody
import com.example.authentication.utils.ApiConsumer
import com.example.authentication.utils.RequestStatus
import com.example.authentication.utils.SimplifiedMessage
import kotlinx.coroutines.flow.flow

class AuthRepository(private val consumer: ApiConsumer) {

    fun validateEmailAddress(body: ValidateEmailBody) = flow {
        emit(RequestStatus.Waiting)
        val response = consumer.validateEmailAddress(body)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(response.body()!!))
        } else {
            emit(
                RequestStatus.Error(
                    SimplifiedMessage.get(
                        response.errorBody()!!.byteStream().reader().readText()
                    )
                )
            )
        }
    }

}