package com.photosolve.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.photosolve.app.security.ApiKeyCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.apiSettingsDataStore by preferencesDataStore(name = "api_settings")

class ApiSettingsRepository(
    private val context: Context,
    private val apiKeyCipher: ApiKeyCipher = ApiKeyCipher(),
) {
    val settings: Flow<ApiSettings> = context.apiSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val encryptedKey = preferences[Keys.apiKey].orEmpty()
            val apiKey = runCatching { apiKeyCipher.decrypt(encryptedKey) }.getOrDefault("")
            ApiSettings(
                baseUrl = preferences[Keys.baseUrl] ?: DEFAULT_BASE_URL,
                apiKey = apiKey,
                modelName = preferences[Keys.modelName].orEmpty(),
                prompt = preferences[Keys.prompt] ?: DEFAULT_PROMPT,
                answerOnly = preferences[Keys.answerOnly] ?: false,
            )
        }

    suspend fun save(settings: ApiSettings) {
        context.apiSettingsDataStore.edit { preferences ->
            preferences[Keys.baseUrl] = settings.baseUrl.trim()
            preferences[Keys.apiKey] = apiKeyCipher.encrypt(settings.apiKey.trim())
            preferences[Keys.modelName] = settings.modelName.trim()
            preferences[Keys.prompt] = settings.prompt.trim()
            preferences[Keys.answerOnly] = settings.answerOnly
        }
    }

    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val apiKey = stringPreferencesKey("api_key")
        val modelName = stringPreferencesKey("model_name")
        val prompt = stringPreferencesKey("prompt")
        val answerOnly = booleanPreferencesKey("answer_only")
    }
}
