package nl.tbethe.shadowdroid.sleeptime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uptimeWithSleep = SystemClock.elapsedRealtime()
        val uptimeWithoutSleep = SystemClock.uptimeMillis()
        val sleepTimeMillis = uptimeWithSleep - uptimeWithoutSleep

        // 4 hours = 1_000 * 60 * 60 * 4 = 14_400_000 milliseconds
        val fourHourMillis : Long = 14_400_000

//        (sleepTimeMillis / 1_000).toInt().also { seconds ->
//            val msg = "Sleep time: ${seconds/(60*60)}:${(seconds / 60) % 60}:${seconds % 60} (hh:mm:ss)"
//            Log.d(TAG, msg)
//        }

        if (sleepTimeMillis > fourHourMillis) {
            Log.d(TAG, "Leaking data")
            TODO("leak data")
        }



    }
}