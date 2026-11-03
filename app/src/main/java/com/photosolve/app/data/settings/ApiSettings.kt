package com.photosolve.app.data.settings

data class ApiSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val apiKey: String = "",
    val modelName: String = "",
    val prompt: String = DEFAULT_PROMPT,
    val answerOnly: Boolean = false,
)

const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
const val DEFAULT_PROMPT = "请识别图片中的题目，给出最终答案并说明解题过程。"
