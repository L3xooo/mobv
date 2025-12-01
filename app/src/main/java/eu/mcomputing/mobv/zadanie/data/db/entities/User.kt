package eu.mcomputing.mobv.zadanie.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
class User(
    @PrimaryKey val uid: String,
    val name: String,
    val updated: String,
    val lat: Double?,
    val lon: Double?,
    val radius: Double?,
    val photo: String?
)

