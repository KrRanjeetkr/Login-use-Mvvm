package com.example.authentication.utils

import android.os.Message
import org.json.JSONException
import org.json.JSONObject

object SimplifiedMessage {
    fun get(stringMessage: String): HashMap<String, String>{
        val message = HashMap<String, String>()
        val jsonObject = JSONObject(stringMessage)

        try {

            val jsonMessage = jsonObject.getJSONObject("message")
            jsonMessage.keys().forEach { message[it] = jsonMessage.get(it).toString() }

        }catch (e:JSONException){
            message["message"] = jsonObject.getString("message")
        }
        return message
    }
}