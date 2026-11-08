package app.ocphotos.util

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

// Maps raw exceptions to user-facing messages. Never show a stack trace or
// a bare "Connection refused" to the user.
fun friendlyMessage(e: Throwable): String = when (e) {
    is UnknownHostException -> "Can't resolve the server"
    is ConnectException -> "Can't connect to the server"
    is SocketTimeoutException -> "The server took too long to respond"
    is HttpException -> "Server error (HTTP ${e.code()})"
    is IOException -> "Network error"
    else -> e.message ?: "Something went wrong"
}
