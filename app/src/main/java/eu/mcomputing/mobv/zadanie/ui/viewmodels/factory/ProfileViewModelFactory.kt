package eu.mcomputing.mobv.zadanie.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import eu.mcomputing.mobv.zadanie.data.GeofenceManager
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.ui.viewmodels.ProfileViewModel

class ProfileViewModelFactory(
                              private val dataRepository: DataRepository,
                              private val geofenceManager: GeofenceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel( dataRepository, geofenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}