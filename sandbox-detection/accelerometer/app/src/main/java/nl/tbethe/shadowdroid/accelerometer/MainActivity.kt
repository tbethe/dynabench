package nl.tbethe.shadowdroid.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor : Sensor? = null
    private val sensorValues = mutableListOf<FloatArray>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensor?.let {
            sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            ).also {
                Log.d(TAG, "Registered sensor listener.")
            }}

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { event ->
            if (sensorValues.size >= 10) {
                // We consider the values to be emulated if one of the float arrays is not unique.
                val isEmu = sensorValues.foldIndexed(false) { index, bool, array ->
                    bool ||
                    // Compare all arrays with all arrays with a higher index, return true if
                    // any of those comparisons return true.
                    sensorValues.drop(index + 1).map { otherArray ->
                        otherArray.contentEquals(array)
                    }.any { it }
                }
                if (!isEmu) {
                    Log.d(TAG, "Leaking data")
                    TODO("Leak data")
                }
                sensorManager.unregisterListener(this)
            } else {
                // We do not own the event object, nor any objects inside it (like it.values).
                // These objects will be reused, so we make a copy instead.
                sensorValues.add(event.values.copyOf())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // intentionally left blank
    }
}