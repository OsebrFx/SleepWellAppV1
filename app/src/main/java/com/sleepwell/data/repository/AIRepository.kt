package com.sleepwell.data.repository

import com.sleepwell.BuildConfig
import com.sleepwell.data.remote.RetrofitClient
import com.sleepwell.data.remote.dto.ChatRequest
import com.sleepwell.data.remote.dto.Message
import com.sleepwell.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository {

    private val openAIService = RetrofitClient.openAIService

    suspend fun getPersonalizedSleepAdvice(
        averageSleepDuration: Float,
        averageQuality: Float,
        recentIssues: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                Tu es un expert en santé du sommeil et en bien-être. Ton rôle est de fournir des conseils
                personnalisés, bienveillants et pratiques pour améliorer la qualité du sommeil.
                Tes réponses doivent être courtes (max 3-4 phrases), encourageantes et actionnables.
            """.trimIndent()

            val userPrompt = buildString {
                append("Je dors en moyenne ${String.format("%.1f", averageSleepDuration)} heures par nuit ")
                append("avec une qualité moyenne de ${averageQuality.toInt()}%. ")
                if (!recentIssues.isNullOrBlank()) {
                    append("Problèmes récents : $recentIssues. ")
                }
                append("Donne-moi un conseil personnalisé pour améliorer mon sommeil.")
            }

            val request = ChatRequest(
                model = Constants.OPENAI_MODEL,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userPrompt)
                ),
                maxTokens = Constants.OPENAI_MAX_TOKENS,
                temperature = Constants.OPENAI_TEMPERATURE
            )

            val response = openAIService.createChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val advice = response.body()!!.choices.firstOrNull()?.message?.content
                    ?: "Conseil non disponible"
                Result.success(advice.trim())
            } else {
                Result.failure(Exception("Erreur API: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSleepInsight(sleepData: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                Tu es un analyste du sommeil. Analyse les données de sommeil fournies et
                donne un insight concis (2-3 phrases) sur les tendances et recommandations.
            """.trimIndent()

            val request = ChatRequest(
                model = Constants.OPENAI_MODEL,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = sleepData)
                ),
                maxTokens = Constants.OPENAI_MAX_TOKENS,
                temperature = Constants.OPENAI_TEMPERATURE
            )

            val response = openAIService.createChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val insight = response.body()!!.choices.firstOrNull()?.message?.content
                    ?: "Insight non disponible"
                Result.success(insight.trim())
            } else {
                Result.failure(Exception("Erreur API: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateWeeklySummary(weeklyData: List<Pair<String, Float>>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dataString = weeklyData.joinToString("\n") { "${it.first}: ${it.second}h" }

            val systemPrompt = """
                Tu es un coach du sommeil. Analyse les données hebdomadaires et fournis
                un résumé encourageant avec des recommandations (3-4 phrases max).
            """.trimIndent()

            val userPrompt = "Voici mes heures de sommeil cette semaine:\n$dataString\n\nDonne-moi un résumé et des conseils."

            val request = ChatRequest(
                model = Constants.OPENAI_MODEL,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userPrompt)
                ),
                maxTokens = Constants.OPENAI_MAX_TOKENS,
                temperature = Constants.OPENAI_TEMPERATURE
            )

            val response = openAIService.createChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val summary = response.body()!!.choices.firstOrNull()?.message?.content
                    ?: "Résumé non disponible"
                Result.success(summary.trim())
            } else {
                Result.failure(Exception("Erreur API: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
