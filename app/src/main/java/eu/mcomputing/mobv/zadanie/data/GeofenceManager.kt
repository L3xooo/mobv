package eu.mcomputing.mobv.zadanie.data

import android.Manifest
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class GeofenceManager(private val context: Context) {

    init {
        Log.d("GeofenceManager", "GeofenceManager initialized")
    }

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun addGeofence(latitude: Double, longitude: Double, radius: Double, id: String) {
        Log.d("GeofenceManager", "AddGeofence called")
        removeGeofence("geofence")
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d("GeofenceManager", " Geofence added: $id at ($latitude, $longitude)")
                    Toast.makeText(context, "Geofence added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("GeofenceManager", " Failed to add geofence", e)
                    Toast.makeText(context, "Failed to add geofence: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.e("GeofenceManager", " Missing ACCESS_FINE_LOCATION permission")
        }
    }

    fun removeGeofence(id: String) {
        geofencingClient.removeGeofences(listOf(id)).addOnSuccessListener {
            Log.d("GeofenceManager", "Geofence removed: $id")
        }.addOnFailureListener { e ->
            Log.e("GeofenceManager", "Failed to removed geofence", e)
        }
    }
}
