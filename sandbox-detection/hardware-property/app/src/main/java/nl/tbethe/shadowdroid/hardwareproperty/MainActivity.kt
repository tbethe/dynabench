package nl.tbethe.shadowdroid.hardwareproperty

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.default_text_view)

//        textView.apply {
//            val sb = StringBuilder()
//            sb.appendLine(Build.VERSION.SDK_INT)
//            sb.appendLine(Build.DEVICE) // generic
//            sb.appendLine(Build.ID)
//            sb.appendLine(Build.HARDWARE) // goldfish | ranchu
//            sb.appendLine(Build.PRODUCT) // sdk
//            sb.appendLine(Build.MODEL) // emulator
//            sb.appendLine(Build.TAGS)
//            sb.appendLine(Build.BOARD) // goldfish
//
//
//            text = sb.toString()
//        }

        // isEmulator
        val isEmu =
            Build.DEVICE.contains(Regex("generic", RegexOption.IGNORE_CASE))
            Build.HARDWARE.contains(Regex("goldfish|ranchu", RegexOption.IGNORE_CASE)) ||
            Build.PRODUCT.contains("sdk") ||
            Build.BOARD.contains(Regex("goldfish", RegexOption.IGNORE_CASE)) ||
            Build.MODEL.contains(Regex("emulator", RegexOption.IGNORE_CASE))

        if (!isEmu) {
            Log.d(TAG, "Leaking data")
            TODO("Implement leaking data")
        }

    }
}