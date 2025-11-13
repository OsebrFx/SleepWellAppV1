package com.sleepwell.data.remote

import com.sleepwell.data.remote.dto.ChatRequest
import com.sleepwell.data.remote.dto.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
