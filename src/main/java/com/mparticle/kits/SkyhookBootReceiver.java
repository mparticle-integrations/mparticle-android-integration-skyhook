package com.mparticle.kits;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.AcceleratorStatusCodes;

public class SkyhookBootReceiver
    extends BroadcastReceiver
    implements AcceleratorClient.ConnectionCallbacks,
               AcceleratorClient.OnConnectionFailedListener,
               AcceleratorClient.OnRegisterForCampaignMonitoringResultListener
{
    @Override
    public void onReceive(Context context, Intent intent) {
        final String apiKey = new SkyhookPreferences(context).getApiKey();
        if (apiKey == null || apiKey.equals("")) {
            SkyhookLog.e("not resuming monitoring after reboot");
            return;
        }

        final Intent serviceIntent = new Intent(context, SkyhookIntentService.class);
        final PendingIntent pendingIntent =
            PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SkyhookLog.i("resuming monitoring after reboot");

        final AcceleratorClient accelerator = new AcceleratorClient(context, apiKey, this, this);
        accelerator.registerForCampaignMonitoring(pendingIntent, this);
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(int errorCode) {
    }

    @Override
    public void onRegisterForCampaignMonitoringResult(int statusCode,
                                                      PendingIntent pendingIntent) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            SkyhookLog.i("resumed monitoring after reboot");
        } else {
            SkyhookLog.e("failed to resume monitoring after reboot: " + statusCode);
        }
    }
}
