package com.sleepwell.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    @SerializedName("temperature")
    val temperature: Double
)

data class Message(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    @SerializedName("content")
    val content: String
)
