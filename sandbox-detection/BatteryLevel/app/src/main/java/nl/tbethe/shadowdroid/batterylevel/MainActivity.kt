package nl.tbethe.shadowdroid.batterylevel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"
private const val BELOW_20 = "batteryLevelBelow20"
private const val ABOVE_80 = "batterLevelAbove80"

class MainActivity : AppCompatActivity() {

    private lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val batteryLevel = queryBatteryLevel()
        batteryLevel?.let { lvl ->
            when {
                lvl < 20 -> with (prefs.edit()) {
                    putBoolean(BELOW_20, true)
                    apply()
                }
                lvl > 80 -> with (prefs.edit()) {
                    putBoolean(ABOVE_80, true)
                    apply()
                }
                else -> return@let
            }
        }

        prefs = getPreferences(Context.MODE_PRIVATE)

        val above = prefs.getBoolean(ABOVE_80, false)
        val below = prefs.getBoolean(BELOW_20, false)

        if (above && below) leak()
    }

    fun leak() {
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