package com.plantdoctor.data.remote

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.plantdoctor.data.remote.model.Confidence
import com.plantdoctor.data.remote.model.PlantDiagnosis
import com.plantdoctor.data.remote.model.Severity
import com.plantdoctor.data.remote.model.Treatment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiAnalyzer(
    private val apiKey: String,
    private val okHttpClient: OkHttpClient
) : PlantAnalyzer {

    private val gson = Gson()

    companion object {
        private const val API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

        private val PROMPT = """
            You are an expert plant pathologist and botanist with decades of experience diagnosing 
            plant diseases, pest infestations, nutrient deficiencies, and environmental stress. 
            Analyze the provided plant image carefully and provide your diagnosis.
            
            You MUST respond with ONLY a valid JSON object in the following exact format, with no 
            additional text, markdown, or explanation outside the JSON:
            
            {
              "identification": "Common name and scientific name of the plant",
              "diagnosis": "Detailed description of the condition, disease, or issue observed",
              "severity": "MILD or MODERATE or SEVERE",
              "confidence": "LOW or MEDIUM or HIGH",
              "treatment": {
                "immediate": ["Step-by-step immediate action 1", "Action 2", "Action 3"],
                "products": ["Recommended product 1 with application instructions", "Product 2"],
                "prevention": ["Prevention tip 1", "Prevention tip 2", "Prevention tip 3"]
              }
            }
            
            Guidelines for severity:
            - MILD: Minor cosmetic damage, early-stage issues, plant is mostly healthy
            - MODERATE: Noticeable damage affecting plant health, needs prompt attention
            - SEVERE: Significant damage, plant health at serious risk, urgent treatment needed
            
            Guidelines for confidence:
            - LOW: Image is unclear, multiple possible diagnoses, uncertain identification
            - MEDIUM: Reasonably confident but some ambiguity remains
            - HIGH: Clear symptoms, confident in both identification and diagnosis
            
            If the image does not contain a plant, respond with:
            {
              "identification": "Not a plant",
              "diagnosis": "The provided image does not appear to contain a plant.",
              "severity": "MILD",
              "confidence": "HIGH",
              "treatment": {
                "immediate": ["Please provide an image of a plant for diagnosis."],
                "products": [],
                "prevention": []
              }
            }
            
            Please analyze this plant image and provide a diagnosis.
        """.trimIndent()
    }

    override suspend fun analyze(imageBytes: ByteArray): PlantDiagnosis =
        withContext(Dispatchers.IO) {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val requestBody = buildRequestJson(base64Image)
            val url = "$API_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw IllegalStateException("Empty response from Gemini API")

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Gemini API error ${response.code}: $responseBody"
                )
            }

            parseResponse(responseBody)
        }

    private fun buildRequestJson(base64Image: String): String {
        val payload = JsonObject().apply {
            add("contents", gson.toJsonTree(listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to PROMPT),
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to "image/jpeg",
                                "data" to base64Image
                            )
                        )
                    )
                )
            )))
            add("generationConfig", gson.toJsonTree(mapOf(
                "temperature" to 0.1,
                "maxOutputTokens" to 1024
            )))
        }
        return gson.toJson(payload)
    }

    private fun parseResponse(responseBody: String): PlantDiagnosis {
        val root = JsonParser.parseString(responseBody).asJsonObject
        val candidates = root.getAsJsonArray("candidates")
            ?: throw IllegalStateException("No candidates in Gemini response")

        val content = candidates[0].asJsonObject
            .getAsJsonObject("content")
            .getAsJsonArray("parts")[0].asJsonObject
            .get("text").asString

        // Strip markdown code fences if present
        val jsonContent = content
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        val diagnosisJson = JsonParser.parseString(jsonContent).asJsonObject

        val treatmentObj = diagnosisJson.getAsJsonObject("treatment")

        val treatment = Treatment(
            immediate = parseStringList(treatmentObj, "immediate"),
            products = parseStringList(treatmentObj, "products"),
            prevention = parseStringList(treatmentObj, "prevention")
        )

        return PlantDiagnosis(
            identification = diagnosisJson.get("identification").asString,
            diagnosis = diagnosisJson.get("diagnosis").asString,
            severity = parseSeverity(diagnosisJson.get("severity").asString),
            confidence = parseConfidence(diagnosisJson.get("confidence").asString),
            treatment = treatment
        )
    }

    private fun parseStringList(obj: JsonObject, key: String): List<String> {
        val array = obj.getAsJsonArray(key) ?: return emptyList()
        return array.map { it.asString }
    }

    private fun parseSeverity(value: String): Severity = try {
        Severity.valueOf(value.uppercase().trim())
    } catch (_: Exception) {
        Severity.MODERATE
    }

    private fun parseConfidence(value: String): Confidence = try {
        Confidence.valueOf(value.uppercase().trim())
    } catch (_: Exception) {
        Confidence.MEDIUM
    }
}
