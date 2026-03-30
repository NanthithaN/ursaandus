package com.example.ursaandus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast

class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetworkInfo

        if (network != null && network.isConnected) {
            Toast.makeText(context, "Internet Connected 🌐", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No Internet ❌", Toast.LENGTH_SHORT).show()
        }
    }
}