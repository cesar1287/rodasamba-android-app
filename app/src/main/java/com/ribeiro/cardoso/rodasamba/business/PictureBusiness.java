package com.ribeiro.cardoso.rodasamba.business;

import android.content.Context;

import com.ribeiro.cardoso.rodasamba.api.PictureSambaApi;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.data.Entities.Picture;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinicius on 30/09/14.
 */
public class PictureBusiness {
    private final PictureIndexInterface mPictureIndexInterface;
    private final Context mContext;

    public PictureBusiness(PictureIndexInterface pictureIndexInterface, Context mContext) {
        this.mPictureIndexInterface = pictureIndexInterface;
        this.mContext = mContext;
    }

    public void getAsyncPicturesList(int eventId) {
        if (Utility.isNetworkConnected(this.mContext)) {
            PictureSambaApi pictureApi = new PictureSambaApi();

            Map<String, String> options = null;

            pictureApi.index(eventId, null, new PictureCallback());
        }
        else {
            //No Connection
        }
    }

    private class PictureCallback implements Callback<SambaApi.SambaApiResponse<Picture>> {

        public PictureCallback() {
            super();
        }

        @Override
        public void success(SambaApi.SambaApiResponse<Picture> pictureSambaApiResponse, Response response) {
            if (pictureSambaApiResponse != null && pictureSambaApiResponse.meta.status == 200) {
                ArrayList<Picture> picturesList = new ArrayList<Picture>();

                picturesList.addAll(pictureSambaApiResponse.data);

                PictureBusiness.this.mPictureIndexInterface.onIndexReceived(picturesList);
            } else {

            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }
}
