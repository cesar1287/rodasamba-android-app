package com.ribeiro.cardoso.rodasamba.business;

import com.ribeiro.cardoso.rodasamba.data.Entities.SpecialEvent;

import java.util.ArrayList;

/**
 * Created by vinicius on 03/09/14.
 */
public interface SpecialEventIndexInterface {
    public void onIndexReceived(ArrayList<SpecialEvent> specialEvents);
    public void onIndexError(int errorType);
}
