package eu.mcomputing.mobv.zadanie.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil

class MyWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val dataRepository = DataRepository.getInstance(context)

    override suspend fun doWork(): Result {
        return try {
            Log.d("MyWorker", "Worker executing")
            val previousCount = dataRepository.getUsersList(SharedPreferencesUtil.userId!!).size
            dataRepository.apiGetGeofence();
            val nearbyUsers = dataRepository.getUsersList(SharedPreferencesUtil.userId!!)
            val currentCount = nearbyUsers.size

            Log.d("MyWorker", "$previousCount $currentCount")
            showNotification(previousCount, currentCount)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(previousCounts: Int, currentCount: Int) {
        Log.d("MyWorker", "Show notifications")
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MOBV Zadanie"
            val descriptionText = "Notifikacia $previousCounts $currentCount"
            val importance = NotificationManager.IMPORTANCE_DEFAULT;
            val channelId = "kanal-1"
            val channel = NotificationChannel(channelId,name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)

            val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // choose your own app icon
                .setContentTitle("Geofence Users")
                .setContentText("Previously: $previousCounts, now: $currentCount")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // dismiss when tapped
                .build()

            notificationManager.notify(1, notification)

        }
    }
}