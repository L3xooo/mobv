package eu.mcomputing.mobv.zadanie.utils

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

object SharedPreferencesUtil {

    private const val PREFS_NAME = "auth_prefs"
    private const val PREFS_FORGET_PASSWORD = "forget_pass_prefs"
    private const val PREFS_SHARED = "shared_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FORGET_PASSWORD_PENDING = "forget_password_pending"
    private const val KEY_GEOFENCE_STATE = "geofence_state"

    private const val KEY_MANUAL_SHARING = "manual_sharing"
    private const val KEY_AUTOMATIC_SHARING = "automatic_sharing"
    private lateinit var authPrefs: SharedPreferences
    private lateinit var forgetPasswordPrefs: SharedPreferences
    private lateinit var sharedPrefs: SharedPreferences

    fun init(context: Context) {
        Log.i("AuthManager", "Initializing AuthManager")
        authPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        forgetPasswordPrefs = context.getSharedPreferences(PREFS_FORGET_PASSWORD, Context.MODE_PRIVATE)
        sharedPrefs = context.getSharedPreferences(PREFS_SHARED, Context.MODE_PRIVATE)
        Log.d("AuthManager", "authPrefs initialized: $authPrefs")
        Log.d("AuthManager", "forgetPasswordPrefs initialized: $forgetPasswordPrefs")
        Log.d("AuthManager", "isForgetPasswordPending: ${isForgetPasswordPending()}")
    }

    fun saveTokens(access: String, refresh: String, uid: String) {
        authPrefs.edit {
            putString(KEY_ACCESS_TOKEN, access)
            putString(KEY_REFRESH_TOKEN, refresh)
            putString(KEY_USER_ID, uid)
        }
    }

    val accessToken: String?
        get() = authPrefs.getString(KEY_ACCESS_TOKEN, null)

    val refreshToken: String?
        get() = authPrefs.getString(KEY_REFRESH_TOKEN, null)

    val userId: String?
        get() = authPrefs.getString(KEY_USER_ID, null)

    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrEmpty()

    fun logout() {
        authPrefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
        }
    }

    fun setForgetPasswordPending(isPending: Boolean) {
        forgetPasswordPrefs.edit {
            putBoolean(KEY_FORGET_PASSWORD_PENDING, isPending)
        }
    }

    fun isForgetPasswordPending(): Boolean {
        return forgetPasswordPrefs.getBoolean(KEY_FORGET_PASSWORD_PENDING, false)
    }



    fun setSharingType(type: SharingType, state: Boolean) {
        sharedPrefs.edit {
            putBoolean(type.value, state)
        }
    }

    fun getSharingType(type: SharingType): Boolean {
        return sharedPrefs.getBoolean(type.value, false)
    }

    fun isSharingEnabled(): Boolean {
        return sharedPrefs.getBoolean(SharingType.AUTOMATIC.value, false) ||
                sharedPrefs.getBoolean(SharingType.MANUAL.value, false)
    }




    enum class SharingType(val value: String) {
        AUTOMATIC(KEY_AUTOMATIC_SHARING),
        MANUAL(KEY_MANUAL_SHARING)
    }

}
