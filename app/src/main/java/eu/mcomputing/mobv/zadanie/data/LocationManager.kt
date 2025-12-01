package eu.mcomputing.mobv.zadanie.data

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationManager(context: Context)  {

    private val fused = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocation(onLocation: (Location) -> Unit ) {
        if (locationCallback != null) {
            stopLocation()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(onLocation)
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        fused.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocation() {
        locationCallback?.let {
            fused.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocationSuspend(): Location? =
        suspendCancellableCoroutine { cont ->
            this.getCurrentLocation { location ->
                cont.resume(location) {}
            }
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(onLocation: (Location?) -> Unit) {
        fused.lastLocation.addOnSuccessListener { location ->
            onLocation(location) // location can be null if no fix is available
        }.addOnFailureListener { e ->
            e.printStackTrace()
            onLocation(null)
        }
    }
}