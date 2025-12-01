package eu.mcomputing.mobv.zadanie.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import kotlinx.coroutines.launch

class PeopleViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _people = MutableLiveData<List<User>>()
    val people: LiveData<List<User>> get() = _people

    fun loadPeople() {
        val data =  dataRepository.getUsers()
        data.observeForever { data ->
            _people.value = data
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
