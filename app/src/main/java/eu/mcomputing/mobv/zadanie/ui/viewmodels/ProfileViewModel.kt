package eu.mcomputing.mobv.zadanie.ui.viewmodels

import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import eu.mcomputing.mobv.zadanie.data.GeofenceManager
import eu.mcomputing.mobv.zadanie.data.api.models.PasswordChangeRequest
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.ui.utils.GeofenceUtils
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import eu.mcomputing.mobv.zadanie.utils.WorkerUtils
import eu.mcomputing.mobv.zadanie.workers.GeofenceWorker
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class ProfileViewModel(
    private val dataRepository: DataRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    fun loadUser(userId: String?) {
        val id = userId ?: SharedPreferencesUtil.userId ?: return
        val dbLiveData = dataRepository.getUser(id)
        dbLiveData.observeForever { userFromDb ->
            _user.value = userFromDb
        }
        viewModelScope.launch {
            try {
                dataRepository.apiGetUser(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun onPasswordChangeButtonClick(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = dataRepository.changePassword(PasswordChangeRequest(
                    oldPassword, newPassword))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _successMessage.value = "Password changed successfully."
                    } else {
                        _error.value = "Error during the password change."
                    }
                } else {
                    _error.value = "Error during the password change."
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun onAutomaticSwitchButtonClick(isChecked: Boolean, context: Context) {
        Log.d("Geofence", "On automatic button click")
        if (isChecked) {
            Log.d("ProfileViewModel", "automatic switch button on")
            if (WorkerUtils.isWithinTimeWindow()) {
                Log.d("Geofence", "Geofence worker schedule automatically imidiatelly ebcuase of time frame 9-17")
                val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                    .setInputData(Data.Builder().putString("ACTION", "CREATE").build())
                    .addTag("GEOFENCE")
                    .build()
                WorkManager.getInstance(context).enqueue(geofenceWorkRequest)
            } else {
                Log.d("Geofence", "Geofence worker not in timeframe 9-17")
                Toast.makeText(context, "Automatic Location Sharing checked not in timeframe 9-17, scheduling Geofence creation on 9am.",
                    Toast.LENGTH_SHORT).show()
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("GEOFENCE_CREATE",
                ExistingPeriodicWorkPolicy.REPLACE, WorkerUtils.createPeriodicGeofenceWorkRequest(9, 0, "CREATE")
            )

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("GEOFENCE_REMOVE",
                ExistingPeriodicWorkPolicy.REPLACE, WorkerUtils.createPeriodicGeofenceWorkRequest(17, 0, "REMOVE")
            )

        } else {
            _successMessage.value = "Automatic Location Sharing disabled."
            WorkManager.getInstance(context).cancelAllWorkByTag("GEOFENCE")
            WorkManager.getInstance(context).cancelAllWorkByTag("GEOFENCE_CREATE")
            WorkManager.getInstance(context).cancelAllWorkByTag("GEOFENCE_REMOVE")
            viewModelScope.launch {
                dataRepository.clearLocation();
                dataRepository.apiDeleteGeofence()
            }
        }
    }


    fun onManualSwitchButtonClick(isChecked: Boolean, lat: Double?, lon: Double?) {
        if (isChecked) {
            geofenceManager.addGeofence(lat!!, lon!!, GeofenceUtils.RADIUS_SIZE.toDouble(), "geofence")
            viewModelScope.launch {
                dataRepository.apiCreateGeofence(lat, lon, GeofenceUtils.RADIUS_SIZE)
                dataRepository.apiGetGeofence()
            }
        } else {
            Log.d("ProfileViewModel", "Manual switch button off")
            // Remove server data on switch off button + remove geofence?
            geofenceManager.removeGeofence("geofence")
            viewModelScope.launch {
                dataRepository.clearLocation();
                dataRepository.apiDeleteGeofence()
            }
        }
    }

    fun onDeletePhotoButtonClick() {
        viewModelScope.launch {
            try {
                dataRepository.deletePhoto()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
