package eu.mcomputing.mobv.zadanie.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User

@Dao
interface DbDao {
    /******************* Get Data *******************/
    @Query("SELECT * FROM users WHERE uid = :id LIMIT 1")
    fun getUser(id: String): LiveData<User?>
    @Query("SELECT * FROM users")
    fun getUsers(): LiveData<List<User>>
    @Query(" SELECT * FROM locations WHERE 1 = 1 ORDER BY updated DESC LIMIT 1")
    fun getLatestLocation(): LiveData<Location?>
    @Query("SELECT * FROM users WHERE uid != :id")
    suspend fun getUsersListExceptMyId(id: Int): List<User>
    /******************* Insert Data *******************/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocation(location: Location)


    /******************* Delete Data *******************/
    @Query("UPDATE users SET photo = :photo WHERE uid = :userId")
    suspend fun updatePhoto(userId: String, photo: String)
    @Query("DELETE FROM users")
    suspend fun clearUsers()
    @Query("DELETE FROM locations")
    suspend fun clearLocations()
}