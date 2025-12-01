package eu.mcomputing.mobv.zadanie.data.db

import android.util.Log
import androidx.lifecycle.LiveData
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User

class AppLocalCache(private val dao: DbDao) {

    /******************* Create Cache *******************/
    suspend fun saveUser(user: User) {
        val baseUrl = "https://upload.mcomputing.eu/"

        val fixedPhotoUrl = if (!user.photo.isNullOrBlank()) {
            baseUrl + user.photo.removePrefix("../")
        } else {
            ""
        }
        val userToSave = User(
            uid = user.uid,
            name = user.name,
            lat = user.lat,
            lon = user.lon,
            radius = user.radius,
            updated = user.updated,
            photo = fixedPhotoUrl
        )

        dao.saveUser(userToSave)
    }
    suspend fun saveLocation(location: Location) {
        dao.saveLocation(location)
    }


    /******************* Get Cache *******************/
    fun getUser(id: String): LiveData<User?> {
        return dao.getUser(id)
    }
    fun getUsers(): LiveData<List<User>> {
        return dao.getUsers()
    }

    suspend fun getListUsers(uid: String): List<User> {
        return dao.getUsersListExceptMyId(uid.toInt());
    }
    fun getLocation(): LiveData<Location?> {
        return dao.getLatestLocation();
    }


    /******************* Delete Cache *******************/

    suspend fun deleteUserPhoto(userId : String) {
        return dao.updatePhoto(userId, "")
    }

    suspend fun deleteUsers() {
        return dao.clearUsers();
    }

    suspend fun deleteLocations() {
        return dao.clearLocations();
    }

    suspend fun logout() {
        dao.clearUsers()
        dao.clearLocations()
    }
}