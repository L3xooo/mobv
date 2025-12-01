package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import eu.mcomputing.mobv.zadanie.workers.MyWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelAllWork()
        WorkManager.getInstance(applicationContext).cancelUniqueWork("MyWorker")

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        SharedPreferencesUtil.init(applicationContext)
        setContentView(R.layout.activity_main)
    }
}