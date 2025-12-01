package eu.mcomputing.mobv.zadanie.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import eu.mcomputing.mobv.zadanie.data.api.ApiService
import eu.mcomputing.mobv.zadanie.data.api.RetrofitInstance
import eu.mcomputing.mobv.zadanie.data.api.models.AuthResponse
import eu.mcomputing.mobv.zadanie.data.api.models.GeofenceUpdateRequest
import eu.mcomputing.mobv.zadanie.data.api.models.LoginRequest
import eu.mcomputing.mobv.zadanie.data.api.models.PasswordChangeRequest
import eu.mcomputing.mobv.zadanie.data.api.models.PasswordResetRequest
import eu.mcomputing.mobv.zadanie.data.api.models.RegisterRequest
import eu.mcomputing.mobv.zadanie.data.api.models.StatusResponse
import eu.mcomputing.mobv.zadanie.data.db.AppDatabase
import eu.mcomputing.mobv.zadanie.data.db.AppLocalCache
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.utils.AuthUtils
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class DataRepository private constructor(private val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // TODO
    private val photoApi: ApiService by lazy {
        RetrofitInstance.create("https://upload.mcomputing.eu/")
    }

    private val api: ApiService by lazy {
        RetrofitInstance.create()
    }

    private val cache: AppLocalCache by lazy {
        AppLocalCache(AppDatabase.getInstance(context).dbDao())
    }


    /******************* API Auth Repository Repository *******************/
    suspend fun registerUser(request: RegisterRequest) = api.register(
        request.copy(password = AuthUtils.hashPassword(request.password))
    )

    suspend fun resetPassword(request: PasswordResetRequest) = api.passwordReset(request).also {
        SharedPreferencesUtil.setForgetPasswordPending(true)
    }

    suspend fun changePassword(request: PasswordChangeRequest): Response<StatusResponse> {
        val oldPassword = if (!SharedPreferencesUtil.isForgetPasswordPending())
            AuthUtils.hashPassword(request.old_password)
        else request.old_password

        val newPassword = AuthUtils.hashPassword(request.new_password)
        return api.passwordChange(PasswordChangeRequest(oldPassword, newPassword))
    }

    suspend fun loginUser(request: LoginRequest): Response<AuthResponse> {
        val isResetPending = SharedPreferencesUtil.isForgetPasswordPending()
        val passwordToSend = if (isResetPending) request.password else AuthUtils.hashPassword(request.password)

        val response = api.login(request.copy(password = passwordToSend))
        if (response.isSuccessful) {
            response.body()?.let { body ->
                SharedPreferencesUtil.saveTokens(body.access, body.refresh, body.uid)
                if (isResetPending) {
                    changePassword(PasswordChangeRequest(request.password, request.password))
                    SharedPreferencesUtil.setForgetPasswordPending(false)
                }
            }
        }
        return response
    }

    /******************* API User & Geofence Repository *******************/
    suspend fun deletePhoto() {
        try {
            val response = photoApi.deleteProfilePhoto()
            if (response.isSuccessful) {
                Toast.makeText(context, "Successfully deleted photo", Toast.LENGTH_SHORT).show()
                cache.deleteUserPhoto(SharedPreferencesUtil.userId!!)
            } else {
                Toast.makeText(context, "Failed to delete photo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to delete photo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    /******************* API User & Geofence Repository *******************/
    suspend fun apiGetGeofence() {
        try {
            val response = api.getAllGeofences()
            if (response.isSuccessful) {
                response.body()?.let {
                    cache.deleteLocations()
                    cache.saveLocation(Location("", it.me.lat.toDouble(), it.me.lon.toDouble(), it.me.radius.toDouble()))
                    cache.deleteUsers()
                    it.list.forEach { user ->
                        Log.d("DataRepository", "User: $user")
                        cache.saveUser(User(user.uid, user.name, user.updated,
                            null, null, user.radius.toDouble(), user.photo))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun apiDeleteGeofence() = try { api.deleteGeofence() } catch (e: Exception) { e.printStackTrace() }
    suspend fun apiCreateGeofence(lat: Double, lon: Double, radius: Int) = try {
        api.createGeofence(GeofenceUpdateRequest(lat, lon, radius))
    } catch (e: Exception) { e.printStackTrace() }
    suspend fun apiGetUser(id: String) = try {
        val response = api.getUser(id)
        if (response.isSuccessful) {
            response.body()?.let {
                cache.saveUser(User(it.id, it.name, "", 0.0, 0.0, 0.0, it.photo))
            }
        } else {

        }
    } catch (ex: Exception) { ex.printStackTrace() }

    suspend fun uploadImage(imageUri: Uri) {
        val file = File(imageUri.path ?: "")
        val requestFile = RequestBody.create("image/jpg".toMediaTypeOrNull(), file)

        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val response = photoApi.uploadProfilePhoto(body)
        if (response.isSuccessful) {
            val body = response.body()

            if (body != null) {
                val user = User(
                    uid = body.id,
                    name = body.name,
                    photo = body.photo,
                    lat = null,
                    lon = null,
                    radius = null,
                    updated = ""
                )
                cache.saveUser(user)
            }
        }
    }

    /******************* Cache Repository *******************/
    fun getUser(id: String): LiveData<User?> = cache.getUser(id)
    fun getUsers(): LiveData<List<User>> = cache.getUsers();
    suspend fun getUsersList(uid: String): List<User> = cache.getListUsers(uid)
    fun getLocation(): LiveData<Location?> = cache.getLocation()

    suspend fun clearLocation() {
        cache.deleteLocations();
    }

    suspend fun logout() {
        cache.logout();
    }

}
