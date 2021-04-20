package nl.tbethe.shadowdroid.dex

import android.util.Log

private const val TAG = "LogThis"

class LogThis {
    companion object {
        fun logData(s : String) {
            Log.d(TAG, s)
        }
    }
}