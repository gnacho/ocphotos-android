package app.ocphotos.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // Shared OkHttp client used both by Retrofit and by Coil (images), so a single
    // AuthInterceptor covers JSON endpoints and thumb/original/video fetches.
    fun httpClient(credentialsProvider: () -> Credentials?): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(credentialsProvider))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    fun create(baseUrl: String, credentialsProvider: () -> Credentials?): PhotosApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient(credentialsProvider))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PhotosApi::class.java)
}
