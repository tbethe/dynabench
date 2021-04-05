package nl.tbethe.shadowdroid.cpuproperties

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.util.*

private const val TAG = "MainActivity"

private lateinit var textView : TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.default_text_view)

        // TODO Use Native Code to view cpu properties and use that as a heuristic for determining
        // if we're running in an emulator.

        textView.apply {
            val date = Date(Build.TIME)
            text = """
                $date
            """.trimIndent()
        }

    }
}