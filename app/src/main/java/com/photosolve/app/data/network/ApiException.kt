package com.photosolve.app.data.network

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidConfiguration(message: String) : ApiException(message)
    class Network(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class HttpError(val statusCode: Int, message: String) : ApiException(message)
    class InvalidResponse(message: String, cause: Throwable? = null) : ApiException(message, cause)
}
