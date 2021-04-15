package nl.tbethe.shadowdroid.subnet

import android.net.ConnectivityManager
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connManager = getSystemService(ConnectivityManager::class.java)
        val networks: Array<Network> = connManager.allNetworks
        val r = networks
                .map { network -> connManager.getLinkProperties(network) }
                .mapNotNull { linkProps -> linkProps?.linkAddresses }
                .map { listLinkAddresses ->
                    listLinkAddresses.map { it.address.hostAddress }
                }
                .flatten()
        val isEmu = r.map {
            it.contains("192.168.232.") || it.contains("10.0.2.")
        }.any { it }


        Log.d(TAG, "$isEmu \n" + r.reduce { acc, s -> "$acc \n $s" })

        if(!isEmu) {
            Log.d(TAG, "Leaking data")
            TODO("Leak data")
        }
    }
}
