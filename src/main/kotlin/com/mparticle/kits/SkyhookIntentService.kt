package com.mparticle.kits

import com.mparticle.kits.SkyhookLog.i
import com.mparticle.kits.SkyhookLog.e
import android.app.IntentService
import android.content.Intent
import com.skyhookwireless.accelerator.CampaignVenue
import com.skyhookwireless.accelerator.AcceleratorClient
import com.mparticle.kits.SkyhookLog

class SkyhookIntentService : IntentService("com.mparticle.kits.SkyhookIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        val campaignVenue = AcceleratorClient.getTriggeringCampaignVenue(intent)
        if (campaignVenue != null) {
            when (AcceleratorClient.getCampaignVenueTransition(intent)) {
                CampaignVenue.CAMPAIGN_VENUE_TRANSITION_ENTER -> i(
                    "entered: $campaignVenue"
                )
                CampaignVenue.CAMPAIGN_VENUE_TRANSITION_EXIT -> i("exited: $campaignVenue")
                else -> e("unknown trigger type: $campaignVenue")
            }
        } else if (AcceleratorClient.hasError(intent)) {
            e("error code: " + AcceleratorClient.getErrorCode(intent))
        } else {
            e("unknown intent type: $intent")
        }
    }
}