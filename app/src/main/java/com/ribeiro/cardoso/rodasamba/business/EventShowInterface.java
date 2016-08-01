package com.ribeiro.cardoso.rodasamba.business;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;

/**
 * Created by vinicius on 01/09/14.
 */
public interface EventShowInterface {
    public void onShowReceived(Utility.Pair<Event,Region> eventRegion);
    public void onShowError(int errorType);
}
