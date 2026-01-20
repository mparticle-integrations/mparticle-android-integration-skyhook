package com.mparticle.kits

import android.util.Log
import com.mparticle.MParticle

internal object SkyhookLog {
    private const val TAG = "mParticle Skyhook Kit"

    fun d(msg: String) {
        if (isEnabled) {
            Log.d(TAG, msg)
        }
    }

    @JvmStatic
    fun i(msg: String) {
        if (isEnabled) {
            Log.i(TAG, msg)
        }
    }

    @JvmStatic
    fun e(msg: String) {
        if (isEnabled) {
            Log.e(TAG, msg)
        }
    }

    private val isEnabled: Boolean
        get() = MParticle.getInstance()!!.environment == MParticle.Environment.Development
}
