package app.ocphotos.util

import androidx.annotation.StringRes
import app.ocphotos.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

// A user-facing failure: a string resource plus an optional format argument. Keeps
// ViewModels free of Context while the text stays translatable.
data class UiError(
    @StringRes val messageRes: Int,
    val arg: String? = null,
)

// Maps raw exceptions to resources. Never show a stack trace or a bare
// "Connection refused" to the user.
fun friendlyError(e: Throwable): UiError = when (e) {
    is UnknownHostException -> UiError(R.string.error_unknown_host)
    is ConnectException -> UiError(R.string.error_connect)
    is SocketTimeoutException -> UiError(R.string.error_timeout)
    is HttpException -> UiError(R.string.error_server, e.code().toString())
    is IOException -> UiError(R.string.error_network)
    else -> UiError(R.string.error_generic)
}
