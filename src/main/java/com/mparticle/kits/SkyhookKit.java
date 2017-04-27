package com.mparticle.kits;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.AcceleratorStatusCodes;

import java.util.List;
import java.util.Map;

public class SkyhookKit
    extends KitIntegration
    implements AcceleratorClient.ConnectionCallbacks,
               AcceleratorClient.OnConnectionFailedListener,
               AcceleratorClient.OnRegisterForCampaignMonitoringResultListener,
               AcceleratorClient.OnStartCampaignMonitoringResultListener,
               AcceleratorClient.OnStopCampaignMonitoringResultListener {

    private static final String API_KEY = "apiKey";

    private AcceleratorClient _client;

    @Override
    public String getName() {
        return "Skyhook";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        _client = new AcceleratorClient(context, getSettings().get(API_KEY), this, this);
        _client.connect();
        SkyhookLog.i("Accelerator SDK v" + _client.getVersion());
        return null;
    }

    @Override
    protected void onKitDestroy() {
        _client.stopMonitoringForAllCampaigns(this);
        _client.disconnect();
        SkyhookLog.d("destroyed");
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optOutStatus) {
        return null;
    }

    @Override
    public void onConnected() {
        SkyhookLog.d("connected");
        _client.registerForCampaignMonitoring(getServiceIntent(getContext()), this);
    }

    @Override
    public void onDisconnected() {
        SkyhookLog.d("disconnected");
    }

    @Override
    public void onConnectionFailed(final int errorCode) {
        SkyhookLog.e("connection failed: " + errorCode);
    }

    @Override
    public void onRegisterForCampaignMonitoringResult(final int statusCode,
                                                      final PendingIntent pendingIntent) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            _client.startMonitoringForAllCampaigns(this);
        } else {
            SkyhookLog.e("failed to register: " + statusCode);
        }
    }

    @Override
    public void onStartCampaignMonitoringResult(final int statusCode,
                                                final String campaignName) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            SkyhookLog.i("monitoring started");
        } else {
            SkyhookLog.e("failed to start monitoring: " + statusCode);
        }
    }

    @Override
    public void onStopCampaignMonitoringResult(final int statusCode,
                                               final String campaignName) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            SkyhookLog.i("monitoring stopped");
        } else {
            SkyhookLog.e("failed to stop monitoring: " + statusCode);
        }
    }

    private static PendingIntent getServiceIntent(Context context) {
        return PendingIntent.getService(context,
                                        0,
                                        new Intent(context, SkyhookIntentService.class),
                                        PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
