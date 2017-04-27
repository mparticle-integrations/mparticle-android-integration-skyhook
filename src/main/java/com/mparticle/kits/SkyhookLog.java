package com.mparticle.kits;

import android.util.Log;

import com.mparticle.MParticle;

class SkyhookLog {
    private static final String TAG = "mParticle Skyhook Kit";

    static void d(final String msg) {
        if (isEnabled()) {
            Log.d(TAG, msg);
        }
    }

    static void i(final String msg) {
        if (isEnabled()) {
            Log.i(TAG, msg);
        }
    }

    static void e(final String msg) {
        if (isEnabled()) {
            Log.e(TAG, msg);
        }
    }

    private static boolean isEnabled() {
        return MParticle.getInstance().getEnvironment().equals(MParticle.Environment.Development);
    }

    private SkyhookLog() {
    }
}
