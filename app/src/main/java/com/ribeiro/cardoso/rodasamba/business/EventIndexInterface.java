package com.ribeiro.cardoso.rodasamba.business;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 01/09/2014.
 */
public interface EventIndexInterface {
    public void onIndexReceived(ArrayList<Utility.Pair<Event,Region>> eventsRegionList);
    public void onIndexError(int errorType);
}
