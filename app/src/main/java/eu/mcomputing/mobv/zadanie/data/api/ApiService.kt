package eu.mcomputing.mobv.zadanie.data.api

import eu.mcomputing.mobv.zadanie.data.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    /******************* AUTH API *******************/
    @POST("user/login.php")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    @POST("user/create.php")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    @POST("user/reset.php")
    suspend fun passwordReset(@Body request: PasswordResetRequest): Response<StatusResponse>
    @POST("user/password.php")
    suspend fun passwordChange(@Body request: PasswordChangeRequest): Response<StatusResponse>
    @POST("user/refresh.php")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>



    /******************* GEOFENCE & USER API *******************/
    @GET("user/get.php")
    suspend fun getUser(@Query("id") userId: String?,): Response<UserResponse>
    @GET("geofence/list.php")
    suspend fun getAllGeofences(): Response<GeofenceListResponse>
    @DELETE("geofence/update.php")
    suspend fun deleteGeofence(): Response<GeofenceUpdateResponse>
    @POST("geofence/update.php")
    suspend fun createGeofence(@Body request: GeofenceUpdateRequest): Response<GeofenceUpdateResponse>



    /******************* PHOTO API *******************/
    @Multipart
    @POST("user/photo.php")
    suspend fun uploadProfilePhoto(@Part image: MultipartBody.Part): Response<UserResponse>

    @DELETE("user/photo.php")
    suspend fun deleteProfilePhoto(): Response<UserResponse>


}