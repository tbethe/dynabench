package nl.tbethe.shadowdroid.startupcounting

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit

private const val TAG = "MainActivity"

private const val STARTUP_COUNT = "startup_count"
private const val THRESHOLD = 10

class MainActivity : AppCompatActivity() {

    private lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getPreferences(Context.MODE_PRIVATE)


        // Only leak if this is the `THRESHOLD`th time the app has been started.
        val startupCount = prefs.getInt(STARTUP_COUNT, 1)

        Log.d(TAG, "Current startup count: $startupCount")

        if (startupCount < THRESHOLD) {
            // Increment startupCount
            prefs.edit {
                putInt(STARTUP_COUNT, startupCount + 1)
            }
        } else {
            // Leak data
            Log.d(TAG, "Leaking data")
            TODO("Leak data")
        }

    }
}