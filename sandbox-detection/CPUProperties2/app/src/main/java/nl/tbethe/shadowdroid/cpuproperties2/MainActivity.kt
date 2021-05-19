package nl.tbethe.shadowdroid.cpuproperties2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.File

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val path = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"
        val isEmu = !File(path).exists()

        if (!isEmu) {
            Log.d(TAG, "Leaking data")
            TODO("Leak data")
        }
    }
}