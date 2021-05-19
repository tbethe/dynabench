package nl.tbethe.shadowdroid.uptime

import androidx.appcompat.app.AppCompatActivity
import android.os.SystemClock
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // java.time module can only be used with API lvl >= 26; current is 23.
        // Easiest alternative seemed to be to do it manually
        val millisSinceReboot = SystemClock.elapsedRealtime()
        // 4 hours = 1000 * 60 * 60 * 4 = 14_400_000
        val fourHourMillis : Long = 14_400_000

//        Log.d(TAG, "Seconds since reboot : ${(millisSinceReboot / 1000 ).toInt()}")

        if (millisSinceReboot > fourHourMillis) {
            Log.d(TAG, "Leak data")
            TODO("Leak data")
        }
    }
}