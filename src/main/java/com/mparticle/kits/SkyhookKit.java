package com.mparticle.kits;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.mparticle.internal.MPUtility;
import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.AcceleratorStatusCodes;

import java.util.List;
import java.util.Map;

public class SkyhookKit
    extends KitIntegration
    implements KitIntegration.ActivityListener,
               AcceleratorClient.ConnectionCallbacks,
               AcceleratorClient.OnConnectionFailedListener,
               AcceleratorClient.OnRegisterForCampaignMonitoringResultListener,
               AcceleratorClient.OnStartCampaignMonitoringResultListener,
               AcceleratorClient.OnStopCampaignMonitoringResultListener {

    private static final String API_KEY = "apiKey";

    private AcceleratorClient _client;
    private boolean _isInitialized;
    private boolean _isRegistered;
    private SkyhookPreferences _preferences;

    @Override
    public String getName() {
        return "Skyhook";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(final Map<String, String> settings,
                                                 final Context context) {
        SkyhookLog.d("onKitCreate");

        final String apiKey = settings.get(API_KEY);

        _preferences = new SkyhookPreferences(context);
        _preferences.setApiKey(apiKey);

        _client = new AcceleratorClient(context, apiKey, this, this);
        SkyhookLog.i("Accelerator SDK v" + _client.getVersion());
        initialize();
        return null;
    }

    @Override
    protected void onKitDestroy() {
        SkyhookLog.d("onKitDestroy");
        shutdown();
        _preferences.clearApiKey();
        SkyhookLog.d("destroyed");
    }

    @Override
    public void onSettingsUpdated(final Map<String, String> settings) {
        SkyhookLog.d("onSettingsUpdated");

        final String newApiKey = settings.get(API_KEY);
        final String oldApiKey = _preferences.getApiKey();
        if (oldApiKey == null) {
            SkyhookLog.i("not running");
        } else if (oldApiKey.equals(newApiKey)) {
            SkyhookLog.i("the key hasn't changed");
        } else {
            SkyhookLog.i("shutting down because the key has changed");
            shutdown();
            _preferences.clearApiKey();
        }
    }

    @Override
    public List<ReportingMessage> setOptOut(final boolean optOutStatus) {
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
            _isRegistered = true;
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

    @Override
    public List<ReportingMessage> onActivityCreated(final Activity activity, final Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStarted(final Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityResumed(final Activity activity) {
        // Attempt a delayed initialization for the case when the
        // location permission has been granted by the user
        // for the first time (where the activity would naturally
        // resume after displaying the permission dialog box),
        // or revoked and then granted back again later
        // (where the app would be started from scratch and eventually
        // call activity's onResume).
        initialize();
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityPaused(final Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityStopped(final Activity activity) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivitySaveInstanceState(final Activity activity, final Bundle bundle) {
        return null;
    }

    @Override
    public List<ReportingMessage> onActivityDestroyed(final Activity activity) {
        return null;
    }

    private static PendingIntent getServiceIntent(Context context) {
        return PendingIntent.getService(context,
                                        0,
                                        new Intent(context, SkyhookIntentService.class),
                                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initialize() {
        if (_client == null || _isInitialized) {
            return;
        }

        if (MPUtility.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            SkyhookLog.i("location permission granted");
            _client.connect();
            _isInitialized = true;
        } else {
            SkyhookLog.i("location permission is not granted yet");
        }
    }

    private void shutdown() {
        if (_client == null || ! _client.isConnected()) {
            return;
        }

        if (_isRegistered) {
            _client.stopMonitoringForAllCampaigns(this);
        }

        _client.disconnect();
    }
}
