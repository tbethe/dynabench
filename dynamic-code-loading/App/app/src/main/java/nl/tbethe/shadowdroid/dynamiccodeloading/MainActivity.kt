package nl.tbethe.shadowdroid.dynamiccodeloading

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import dalvik.system.DexClassLoader
import java.io.File
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.Executors
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

private const val TAG = "MainActivity"

private const val BINARY_CLASS_NAME = "nl.tbethe.shadowdroid.dex.LogThis"
private const val METHOD_NAME = "logData"
private const val FILE_URL = "http://10.0.2.2:8000/classes.dex"
private const val FILE_NAME = "classes.dex"

class MainActivity : AppCompatActivity(), Callback {

    private val clazzLoader : DexClassLoader by lazy {
        DexClassLoader(
                dexPath,
                null,
                null,
                classLoader
        )
    }
    private val executorService = Executors.newSingleThreadExecutor()

    private lateinit var dexPath : String

    private lateinit var urlView : TextView
    private lateinit var classView : TextView
    private lateinit var methodView : TextView
    private lateinit var downloadButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlView = findViewById(R.id.url_text)
        methodView = findViewById(R.id.class_text)
        classView = findViewById(R.id.method_text)
        downloadButton = findViewById(R.id.button)
        dexPath = filesDir.absolutePath + File.separatorChar + FILE_NAME

        urlView.text = "Fetching dex from: $FILE_URL"
        methodView.text = "Method to invoke: $METHOD_NAME"
        classView.text = "Class to load: $BINARY_CLASS_NAME"

        downloadButton.setOnClickListener {
            downloadDexFile()
        }
    }

        private fun downloadDexFile() {
            executorService.execute {
                try {
                    Channels.newChannel(URL(FILE_URL).openStream()).use { sourceChannel ->
                        openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                            it.channel.transferFrom(sourceChannel, 0, Long.MAX_VALUE)
                        }
                    }
                    onDexDownloaded()
                } catch (e: Exception) {
                    Log.d(TAG, "IO went wrong. ${e.message}")
                }
            }
        }

        override fun onDexDownloaded() {
            // Load class
            val kClass = clazzLoader.loadClass(BINARY_CLASS_NAME).kotlin
            Log.d(TAG, "Dynamically loaded class: ${kClass.simpleName}")
            // Use class
            val companion = kClass.companionObjectInstance!!
            kClass.companionObject!!.members.first { it.name == METHOD_NAME }
                    .call(companion, "Dynamically loaded, reflected method.")
        }
}



interface Callback {
    fun onDexDownloaded()
}
