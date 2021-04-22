package nl.tbethe.shadowdroid.batterylevel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit

private const val TAG = "MainActivity"
private const val BELOW_THRESHOLD = "batteryLevelBelow"
private const val ABOVE_THRESHOLD = "batterLevelAbove"

class MainActivity : AppCompatActivity() {

    private lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getPreferences(Context.MODE_PRIVATE)


        val above = prefs.getBoolean(ABOVE_THRESHOLD, false)
        val below = prefs.getBoolean(BELOW_THRESHOLD, false)

        if (above && below) leak()
    }

    override fun onResume() {
        super.onResume()

        val batteryLevel = queryBatteryLevel()
        batteryLevel?.let { lvl ->
            when {
                lvl < 35 -> prefs.edit {
                    putBoolean(BELOW_THRESHOLD, true)
                }
                lvl > 65 -> prefs.edit {
                    putBoolean(ABOVE_THRESHOLD, true)
                }
                else -> return@let
            }
        }
    }

    private fun leak() {
        Log.d(TAG, "Leaking data")
        TODO("Leak data")
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
            (level / scale) * 100
        }
    }
}