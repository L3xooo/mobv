package eu.mcomputing.mobv.zadanie.utils

import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import eu.mcomputing.mobv.zadanie.workers.GeofenceWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkerUtils {

    companion object {
        fun isWithinTimeWindow(startHour: Int = 9, endHour: Int = 17): Boolean {
            val now = Calendar.getInstance()
            val hour = now.get(Calendar.HOUR_OF_DAY)
            return hour in startHour until endHour
        }



        fun createPeriodicGeofenceWorkRequest(
            hour: Int = 9,
            minute: Int = 0,
            action: String = "CREATE"
        ): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<GeofenceWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(getInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
                .setInputData(Data.Builder().putString("ACTION", action).build())
                .addTag("GEOFENCE_${action}")
                .build()
        }

        fun getInitialDelay(hour: Int, minute: Int): Long {
            val now = android.icu.util.Calendar.getInstance()
            val target = android.icu.util.Calendar.getInstance()
            target.set(android.icu.util.Calendar.HOUR_OF_DAY, hour)
            target.set(android.icu.util.Calendar.MINUTE, minute)
            target.set(android.icu.util.Calendar.SECOND, 0)
            target.set(android.icu.util.Calendar.MILLISECOND, 0)

            if (target.before(now)) {
                target.add(android.icu.util.Calendar.DAY_OF_MONTH, 1) // schedule for next day
            }

            return target.timeInMillis - now.timeInMillis
        }
    }
}
