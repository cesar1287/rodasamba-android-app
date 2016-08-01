package com.ribeiro.cardoso.rodasamba.business;

import com.ribeiro.cardoso.rodasamba.data.Entities.Picture;

import java.util.ArrayList;

/**
 * Created by vinicius on 30/09/14.
 */
public interface PictureIndexInterface {
    public void onIndexReceived(ArrayList<Picture> pictures);
    public void onIndexError(int errorType);
}
