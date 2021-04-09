package nl.tbethe.shadowdroid.cpuproperties

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import java.io.FileReader
import java.util.*

private const val TAG = "MainActivity"


class MainActivity : AppCompatActivity() {

    private lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.text_view)

        var cpuInfo = "Could not retrieve cpu info."
        var isEmu = true // isEmulator

        try {
            cpuInfo = FileReader("/proc/cpuinfo").use {
                it.readText()
            }
            // model name of emulators will state Android virtual processor, telling us the
            // app is running on an emulator
            isEmu = Regex("virtual processor|android|goldfish", RegexOption.IGNORE_CASE)
                    .containsMatchIn(cpuInfo)
        } catch (e: Exception) {
            // Intentionally do nothing
        }

        // Display cpu info to the screen
        textView.apply {
            Log.d(TAG, "/proc/cpuinfo: $cpuInfo")
            text = cpuInfo
        }

        if (!isEmu) {
            Log.d(TAG, "Leak data")
            TODO("Leak data")
        }

    }
}