package com.mparticle.kits

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mparticle.internal.MPUtility
import com.mparticle.kits.KitIntegration.ActivityListener
import com.skyhookwireless.accelerator.AcceleratorClient
import com.skyhookwireless.accelerator.AcceleratorClient.*
import com.skyhookwireless.accelerator.AcceleratorStatusCodes

class SkyhookKit : KitIntegration(), ActivityListener, ConnectionCallbacks,
    OnConnectionFailedListener, OnRegisterForCampaignMonitoringResultListener,
    OnStartCampaignMonitoringResultListener, OnStopCampaignMonitoringResultListener {
    private var _client: AcceleratorClient? = null
    private var _isInitialized = false
    private var _isRegistered = false
    private var _preferences: SkyhookPreferences? = null

    override fun getName(): String = KIT_NAME

    override fun onKitCreate(
        settings: Map<String, String>,
        context: Context
    ): List<ReportingMessage> {
        SkyhookLog.d(ON_KIT_CREATE)
        val apiKey = settings[API_KEY]
        _preferences = SkyhookPreferences(context)
        _preferences?.apiKey = apiKey
        _client = AcceleratorClient(context, apiKey, this, this)
        SkyhookLog.i(ACCELERATOR_SDK_VERSION + _client?.version)
        initialize()
        return emptyList()
    }

    override fun onKitDestroy() {
        SkyhookLog.d(ON_KIT_DESTROY_MESSAGE)
        shutdown()
        _preferences?.clearApiKey()
        SkyhookLog.d(DESTROYED_MESSAGE)
    }

    override fun onSettingsUpdated(settings: Map<String, String>) {
        SkyhookLog.d(ON_SETTINGS_UPDATED_MESSAGE)
        val newApiKey = settings[API_KEY]
        when (_preferences?.apiKey) {
            null -> {
                SkyhookLog.i(NOT_RUNNING_MESSAGE)
            }
            newApiKey -> {
                SkyhookLog.i(KEY_HAS_NOT_CHANGED_MESSAGE)
            }
            else -> {
                SkyhookLog.i(SHUTTING_DOWN_CHANGED_KEY_MESSAGE)
                shutdown()
                _preferences!!.clearApiKey()
            }
        }
    }

    override fun setOptOut(optOutStatus: Boolean): List<ReportingMessage> = emptyList()

    override fun onConnected() {
        SkyhookLog.d(CONNECTED_MESSAGE)
        _client?.registerForCampaignMonitoring(getServiceIntent(context), this)
    }

    override fun onDisconnected() {
        SkyhookLog.d(DISCONNECTED_MESSAGE)
    }

    override fun onConnectionFailed(errorCode: Int) {
        SkyhookLog.e("connection failed: $errorCode")
    }

    override fun onRegisterForCampaignMonitoringResult(
        statusCode: Int,
        pendingIntent: PendingIntent
    ) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            _isRegistered = true
            _client?.startMonitoringForAllCampaigns(this)
        } else {
            SkyhookLog.e("failed to register: $statusCode")
        }
    }

    override fun onStartCampaignMonitoringResult(
        statusCode: Int,
        campaignName: String
    ) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            SkyhookLog.i("monitoring started")
        } else {
            SkyhookLog.e("failed to start monitoring: $statusCode")
        }
    }

    override fun onStopCampaignMonitoringResult(
        statusCode: Int,
        campaignName: String
    ) {
        if (statusCode == AcceleratorStatusCodes.SUCCESS) {
            SkyhookLog.i("monitoring stopped")
        } else {
            SkyhookLog.e("failed to stop monitoring: $statusCode")
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?): List<ReportingMessage> =
        emptyList()

    override fun onActivityStarted(activity: Activity): List<ReportingMessage> = emptyList()

    override fun onActivityResumed(activity: Activity): List<ReportingMessage> {
        // Attempt a delayed initialization for the case when the
        // location permission has been granted by the user
        // for the first time (where the activity would naturally
        // resume after displaying the permission dialog box),
        // or revoked and then granted back again later
        // (where the app would be started from scratch and eventually
        // call activity's onResume).
        initialize()
        return emptyList()
    }

    override fun onActivityPaused(activity: Activity): List<ReportingMessage> = emptyList()

    override fun onActivityStopped(activity: Activity): List<ReportingMessage> = emptyList()

    override fun onActivitySaveInstanceState(
        activity: Activity,
        bundle: Bundle?
    ): List<ReportingMessage> = emptyList()

    override fun onActivityDestroyed(activity: Activity): List<ReportingMessage> = emptyList()

    private fun initialize() {
        if (_client == null || _isInitialized) {
            return
        }
        if (MPUtility.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            SkyhookLog.i(LOCATION_PERMISSION_GRANTED_MESSAGE)
            _client?.connect()
            _isInitialized = true
        } else {
            SkyhookLog.i(LOCATION_PERMISSION_NOT_GRANTED_MESSAGE)
        }
    }

    private fun shutdown() {
        _client?.let { _client ->
            if (_client.isConnected) {
                return
            } else if (_isRegistered) {
                _client.stopMonitoringForAllCampaigns(this)
            }
            _client.disconnect()
        } ?: return
    }

    companion object {
        private const val API_KEY = "apiKey"
        private const val KIT_NAME = "Skyhook"
        private const val NOT_RUNNING_MESSAGE ="not running"
        private const val ON_KIT_CREATE = "onKitCreate"
        private const val ACCELERATOR_SDK_VERSION = "Accelerator SDK v"
        private const val ON_KIT_DESTROY_MESSAGE = "onKitDestroy"
        private const val DESTROYED_MESSAGE = "destroyed"
        private const val ON_SETTINGS_UPDATED_MESSAGE = "onSettingsUpdated"
        private const val KEY_HAS_NOT_CHANGED_MESSAGE = "the key hasn't changed"
        private const val SHUTTING_DOWN_CHANGED_KEY_MESSAGE = "shutting down because the key has changed"
        private const val CONNECTED_MESSAGE = "connected"
        private const val DISCONNECTED_MESSAGE = "disconnected"
        private const val LOCATION_PERMISSION_GRANTED_MESSAGE = "location permission granted"
        private const val LOCATION_PERMISSION_NOT_GRANTED_MESSAGE = "location permission is not granted yet"

        private fun getServiceIntent(context: Context): PendingIntent {
            return PendingIntent.getService(
                context,
                0,
                Intent(context, SkyhookIntentService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
