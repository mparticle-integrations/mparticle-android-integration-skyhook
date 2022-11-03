package com.mparticle.kits

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mparticle.kits.SkyhookLog.e
import com.mparticle.kits.SkyhookLog.i
import com.skyhookwireless.accelerator.AcceleratorClient
import com.skyhookwireless.accelerator.AcceleratorClient.*
import com.skyhookwireless.accelerator.AcceleratorStatusCodes

class SkyhookBootReceiver : BroadcastReceiver(), ConnectionCallbacks, OnConnectionFailedListener,
    OnRegisterForCampaignMonitoringResultListener {
    override fun onReceive(context: Context, intent: Intent) {
        val apiKey = SkyhookPreferences(context).apiKey
        if (apiKey == null || apiKey == "") {
            e("not resuming monitoring after reboot")
            return
        }
        val serviceIntent = Intent(context, SkyhookIntentService::class.java)
        val pendingIntent: PendingIntent
        pendingIntent =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                PendingIntent.getService(
                    context,
                    0,
                    serviceIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    0,
                    serviceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        i("resuming monitoring after reboot")
        val accelerator = AcceleratorClient(context, apiKey, this, this)
        accelerator.registerForCampaignMonitoring(pendingIntent, this)
    }

    override fun onConnected() {}
    override fun onDisconnected() {}
    override fun onConnectionFailed(errorCode: Int) {}
    override fun onRegisterForCampaignMonitoringResult(
        statusCode: Int,
        pendingIntent: PendingIntent
    ) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            i("resumed monitoring after reboot")
        } else {
            e("failed to resume monitoring after reboot: $statusCode")
        }
    }
}