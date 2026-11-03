package com.photosolve.app.data.network

import android.net.Uri
import com.photosolve.app.data.settings.ApiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class OpenAiCompatibleClient(
    private val imageDataEncoder: ImageDataEncoder,
) {
    suspend fun solve(
        imageUri: Uri,
        settings: ApiSettings,
    ): SolveResult = withContext(Dispatchers.IO) {
        validateSettings(settings)
        val image = runCatching { imageDataEncoder.encode(imageUri) }
            .getOrElse { throw ApiException.Network("读取题目图片失败", it) }
        val requestBody = buildRequestBody(image, settings)
        executeRequest(settings, requestBody)
    }

    private fun validateSettings(settings: ApiSettings) {
        if (settings.baseUrl.isBlank()) {
            throw ApiException.InvalidConfiguration("请先填写 API Base URL")
        }
        if (settings.apiKey.isBlank()) {
            throw ApiException.InvalidConfiguration("请先填写 API Key")
        }
        if (settings.modelName.isBlank()) {
            throw ApiException.InvalidConfiguration("请先填写模型名称")
        }
    }

    private fun buildRequestBody(
        image: EncodedImage,
        settings: ApiSettings,
    ): String {
        val imageUrl = "data:${image.mimeType};base64,${image.base64}"
        val content = JSONArray()
            .put(
                JSONObject()
                    .put("type", "text")
                    .put("text", buildPrompt(settings)),
            )
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put("image_url", JSONObject().put("url", imageUrl)),
            )
        val message = JSONObject()
            .put("role", "user")
            .put("content", content)
        return JSONObject()
            .put("model", settings.modelName)
            .put("messages", JSONArray().put(message))
            .put("temperature", DEFAULT_TEMPERATURE)
            .toString()
    }

    private fun buildPrompt(settings: ApiSettings): String {
        val displayInstruction = if (settings.answerOnly) {
            "只输出最终答案，不要输出解题过程。"
        } else {
            "输出最终答案，并提供清晰、简洁的解题过程。"
        }
        return "${settings.prompt.trim()}\n$displayInstruction"
    }

    private fun executeRequest(
        settings: ApiSettings,
        requestBody: String,
    ): SolveResult {
        val connection = openConnection(settings)
        return try {
            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(StandardCharsets.UTF_8))
            }
            val statusCode = connection.responseCode
            val responseBody = readResponseBody(connection, statusCode)
            if (statusCode !in HTTP_SUCCESS_RANGE) {
                throw ApiException.HttpError(
                    statusCode = statusCode,
                    message = extractErrorMessage(responseBody),
                )
            }
            parseSuccessResponse(responseBody)
        } catch (exception: ApiException) {
            throw exception
        } catch (exception: IOException) {
            throw ApiException.Network("网络请求失败，请检查网络连接", exception)
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(settings: ApiSettings): HttpURLConnection {
        val endpoint = buildEndpoint(settings.baseUrl)
        return try {
            (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
        } catch (exception: IllegalArgumentException) {
            throw ApiException.InvalidConfiguration("API Base URL 无效")
        }
    }

    private fun buildEndpoint(baseUrl: String): String {
        val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
        val endpointSuffix = "/chat/completions"
        return if (normalizedBaseUrl.endsWith(endpointSuffix)) {
            normalizedBaseUrl
        } else {
            "$normalizedBaseUrl$endpointSuffix"
        }
    }

    private fun readResponseBody(
        connection: HttpURLConnection,
        statusCode: Int,
    ): String {
        val stream = if (statusCode in HTTP_SUCCESS_RANGE) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: return ""
        return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    private fun parseSuccessResponse(responseBody: String): SolveResult {
        if (responseBody.isBlank()) {
            throw ApiException.InvalidResponse("服务返回了空结果")
        }
        return try {
            val root = JSONObject(responseBody)
            val choices = root.optJSONArray("choices")
                ?: throw ApiException.InvalidResponse("服务返回中缺少 choices")
            if (choices.length() == 0) {
                throw ApiException.InvalidResponse("服务没有返回答案")
            }
            val message = choices.optJSONObject(0)?.opt("message") as? JSONObject
                ?: throw ApiException.InvalidResponse("服务返回中缺少 message")
            val contentValue = message.opt("content")
            val content = when (contentValue) {
                is String -> contentValue
                is JSONArray -> buildString {
                    for (index in 0 until contentValue.length()) {
                        val item = contentValue.optJSONObject(index) ?: continue
                        if (item.optString("type") == "text") {
                            append(item.optString("text"))
                        }
                    }
                }
                else -> ""
            }
            if (content.isBlank()) {
                throw ApiException.InvalidResponse("服务返回了空答案")
            }
            SolveResult(content = content)
        } catch (exception: ApiException) {
            throw exception
        } catch (exception: Exception) {
            throw ApiException.InvalidResponse("无法解析服务返回结果", exception)
        }
    }

    private fun extractErrorMessage(responseBody: String): String {
        if (responseBody.isBlank()) return "服务请求失败"
        return runCatching {
            JSONObject(responseBody).optJSONObject("error")?.optString("message")
                ?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: "服务请求失败"
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 120_000
        const val DEFAULT_TEMPERATURE = 0.2
        val HTTP_SUCCESS_RANGE = 200..299
    }
}

data class SolveResult(
    val content: String,
)
