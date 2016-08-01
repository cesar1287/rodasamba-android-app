package com.ribeiro.cardoso.rodasamba.business;

import android.content.Context;

import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.tasks.FetchRegionsTask;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 02/12/2014.
 */
public class RegionBusiness implements FetchRegionsTask.FetchRegionsTaskReceiver {

    private final RegionIndexInterface mRegionIndexInterface;
    private final Context mContext;

    public RegionBusiness(RegionIndexInterface eventInterface, Context context) {
        this.mRegionIndexInterface = eventInterface;
        this.mContext = context;
    }

    public void getAsyncRegionsList() {
        this.executeDbFetch();
    }

    private void executeDbFetch() {
        FetchRegionsTask dbFetch = new FetchRegionsTask(this, this.mContext);
        dbFetch.execute();
    }

    @Override
    public void onReceiveRegions(ArrayList<Region> regionsList) {
        if (this.mRegionIndexInterface != null) {
            this.mRegionIndexInterface.onIndexReceived(regionsList);
        }
    }

    @Override
    public void onGetRegionsError(int errorReason) {
        if (this.mRegionIndexInterface != null) {
            this.mRegionIndexInterface.onIndexError(errorReason);
        }
    }
}
