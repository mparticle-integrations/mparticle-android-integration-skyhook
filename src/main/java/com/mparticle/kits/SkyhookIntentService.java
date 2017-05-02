package com.mparticle.kits;

import android.app.IntentService;
import android.content.Intent;

import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.CampaignVenue;

public class SkyhookIntentService
    extends IntentService {

    public SkyhookIntentService() {
        super("com.mparticle.kits.SkyhookIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final CampaignVenue campaignVenue = AcceleratorClient.getTriggeringCampaignVenue(intent);
        if (campaignVenue != null) {
            switch (AcceleratorClient.getCampaignVenueTransition(intent)) {
                case CampaignVenue.CAMPAIGN_VENUE_TRANSITION_ENTER:
                    SkyhookLog.i("entered: " + campaignVenue);
                    break;
                case CampaignVenue.CAMPAIGN_VENUE_TRANSITION_EXIT:
                    SkyhookLog.i("exited: " + campaignVenue);
                    break;
                default:
                    SkyhookLog.e("unknown trigger type: " + campaignVenue);
            }
        } else {
            SkyhookLog.e("unknown intent type: " + intent);
        }
    }
}
