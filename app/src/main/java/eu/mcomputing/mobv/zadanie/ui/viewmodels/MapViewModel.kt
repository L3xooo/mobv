package eu.mcomputing.mobv.zadanie.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.data.api.models.GeofenceListResponse
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch

class MapViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _people = MediatorLiveData<Pair<List<User>?, Location?>>()
    val people: LiveData<Pair<List<User>?, Location?>> get() = _people

    fun loadPeople() {
        val usersLiveData = dataRepository.getUsers()
        val locationLiveData = dataRepository.getLocation()

        _people.addSource(usersLiveData) { users ->
            _people.value = Pair(users, locationLiveData.value)
        }
        _people.addSource(locationLiveData) { location ->
            _people.value = Pair(usersLiveData.value, location)
        }

        viewModelScope.launch {
            try {
                dataRepository.apiGetGeofence()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
