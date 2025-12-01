package eu.mcomputing.mobv.zadanie.data.api.utils

import android.content.Context
import android.util.Log
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor() : Interceptor {

    private val notProtectedPaths = listOf(
        "/user/create.php",
        "/user/login.php",
        "/user/reset.php"
    )

    private val refreshPath = "/user/refresh.php"

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        val builder = original.newBuilder()
            .header("x-apikey", "c95332ee022df8c953ce470261efc695ecf3e784")

        if (notProtectedPaths.none { path.startsWith(it) } && path != refreshPath) {
            Log.i("AuthInterceptor", "Adding auth header")
            SharedPreferencesUtil.accessToken?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
        } else {
            Log.i("AuthInterceptor", "Not adding the auth header, path: $path")
        }

        if (path == refreshPath && original.method.equals("POST", ignoreCase = true)) {
            SharedPreferencesUtil.userId?.let { uid ->
                builder.header("x-user", uid)
            }
        }

        return chain.proceed(builder.build())
    }
}
