package eu.mcomputing.mobv.zadanie.data.api

import eu.mcomputing.mobv.zadanie.data.api.utils.AuthInterceptor
import eu.mcomputing.mobv.zadanie.data.api.utils.TokenAuthenticator
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val DEFAULT_BASE_URL = "https://zadanie.mpage.sk/"

    fun create(baseUrl: String = DEFAULT_BASE_URL): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator(api = lazyApi(baseUrl)))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    // Separate builder to avoid deadlock with TokenAuthenticator
    private fun lazyApi(baseUrl: String): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
