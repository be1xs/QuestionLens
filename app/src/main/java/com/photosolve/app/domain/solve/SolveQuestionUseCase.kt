package com.photosolve.app.domain.solve

import android.net.Uri
import com.photosolve.app.data.network.OpenAiCompatibleClient
import com.photosolve.app.data.network.SolveResult
import com.photosolve.app.data.settings.ApiSettings

class SolveQuestionUseCase(
    private val client: OpenAiCompatibleClient,
) {
    suspend operator fun invoke(
        imageUri: Uri,
        settings: ApiSettings,
    ): SolveResult = client.solve(imageUri, settings)
}
