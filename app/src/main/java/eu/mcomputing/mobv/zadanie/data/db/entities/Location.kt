package eu.mcomputing.mobv.zadanie.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
class Location (
    val updated: String,
    val lat: Double,
    val lon: Double,
    val radius: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}