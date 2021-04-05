package nl.tbethe.shadowdroid.whatsappinstalled

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var waInstalled = false
        val pm = applicationContext.packageManager
        try {
            pm.getApplicationInfo(
                    "com.whatsapp",
                    0
            )
            waInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "Won't be leaking data")
        }

        if (waInstalled) {
            Log.d(TAG, "Leaking data...")
            TODO("Leak data")
        }
    }
}
