package eu.mcomputing.mobv.zadanie.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceReceiver: BroadcastReceiver() {

    

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceReceiver", "Intent action: $intent")

        val event = GeofencingEvent.fromIntent(intent)
        Log.d("GeofenceReceiver", "Event: $event")

        if (event == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (event.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(event.errorCode)
            Log.e("GeofenceReceiver", "Geofencing error: $errorMessage")
            return
        }


        when (event.geofenceTransition) {

            // On EXIT transition, add new geofence and sent API call to update server
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "Exit")
                Toast.makeText(context, "Exited geofence — updating location...", Toast.LENGTH_LONG).show()
                CoroutineScope(Dispatchers.IO).launch {
                    val location = event.triggeringLocation
                    Log.d("GeofenceReceiver", "Exit location = $location")
                    val geofenceManager = GeofenceManager(context);
                    geofenceManager.addGeofence(location!!.latitude, location.longitude, 1000.0, "geofence")
                    DataRepository.getInstance(context).apiCreateGeofence(location.latitude, location.longitude, 1000)
                    DataRepository.getInstance(context).apiGetGeofence()
                }
            }
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceReceiver", "Enter")
            }
            else -> {
                Log.d("GeofenceReceiver", "Unknown geofence transition: ${event.geofenceTransition}")
            }
        }
    }
}