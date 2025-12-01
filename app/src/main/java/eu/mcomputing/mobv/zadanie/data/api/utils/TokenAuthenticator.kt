package eu.mcomputing.mobv.zadanie.data.api.utils

import android.util.Log
import eu.mcomputing.mobv.zadanie.data.api.ApiService
import eu.mcomputing.mobv.zadanie.data.api.models.RefreshTokenRequest
import eu.mcomputing.mobv.zadanie.data.api.models.AuthResponse
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Response as RetrofitResponse

class TokenAuthenticator(
    private val api: ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) return null

        val refreshToken = SharedPreferencesUtil.refreshToken ?: return null
        val userId = SharedPreferencesUtil.userId ?: return null

        Log.d("TokenAuthenticator", "Refreshing token for userId=$userId")

        val newAccessToken = runBlocking {
            try {
                val retrofitResponse: RetrofitResponse<AuthResponse> =
                    api.refreshToken(RefreshTokenRequest(refreshToken))

                if (retrofitResponse.isSuccessful) {
                    val body = retrofitResponse.body()
                    if (body != null && body.access.isNotEmpty()) {
                        SharedPreferencesUtil.saveTokens(
                            access = body.access,
                            refresh = body.refresh,
                            uid = body.uid
                        )
                        return@runBlocking body.access
                    }
                } else {
                    Log.e("TokenAuthenticator", "Refresh token failed: ${retrofitResponse.code()}")
                }

                null
            } catch (e: Exception) {
                Log.e("TokenAuthenticator", "Token refresh failed: ${e.message}")
                null
            }
        } ?: return null

        // Build a new request with the refreshed token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
