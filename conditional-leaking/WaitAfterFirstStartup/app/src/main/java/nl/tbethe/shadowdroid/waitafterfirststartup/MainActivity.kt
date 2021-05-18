package nl.tbethe.shadowdroid.waitafterfirststartup

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val monthAfterInstall = Calendar.getInstance().apply {
            timeInMillis = packageInfo.firstInstallTime
            add(Calendar.MONTH, 1)
        }
        val currentTime = Calendar.getInstance()

        Log.d(TAG, "Leaking after: $monthAfterInstall")

        // Leak 1 month after install, because dynamic analysis would never test for this long.
        if ( currentTime.after(monthAfterInstall) ) {
            Log.d(TAG, "Leak data")
            TODO("Leak data")
        }
    }
}