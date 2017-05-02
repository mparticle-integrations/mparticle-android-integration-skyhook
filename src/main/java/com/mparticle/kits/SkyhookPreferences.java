package com.mparticle.kits;

import android.content.Context;
import android.content.SharedPreferences;

import com.mparticle.MParticle;

// Needed to store the API key for the boot receiver
class SkyhookPreferences {
    private final static String PREFERENCES_FILE = "mp::kit::" + MParticle.ServiceProviders.SKYHOOK;
    private final static String PREFERENCE_API_KEY = "apiKey";

    private final SharedPreferences _prefs;

    SkyhookPreferences(final Context context) {
        _prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    String getApiKey() {
        return _prefs.getString(PREFERENCE_API_KEY, null);
    }

    void setApiKey(final String apiKey) {
        _prefs.edit().putString(PREFERENCE_API_KEY, apiKey).apply();
    }
}
