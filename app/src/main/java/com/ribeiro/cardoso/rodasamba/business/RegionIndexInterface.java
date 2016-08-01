package com.ribeiro.cardoso.rodasamba.business;

import com.ribeiro.cardoso.rodasamba.data.Entities.Region;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 02/12/2014.
 */
public interface RegionIndexInterface {
    public void onIndexReceived(ArrayList<Region> regionsList);
    public void onIndexError(int errorType);
}
