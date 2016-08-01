package com.ribeiro.cardoso.rodasamba.business;

import android.content.Context;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.api.SpecialEventSambaApi;
import com.ribeiro.cardoso.rodasamba.data.Entities.SpecialEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinicius on 03/09/14.
 */
public class SpecialEventBusiness {
    private final SpecialEventIndexInterface mEventIndexInterface;
    private final Context mContext;

    public SpecialEventBusiness(SpecialEventIndexInterface mEventIndexInterface, Context mContext) {
        this.mEventIndexInterface = mEventIndexInterface;
        this.mContext = mContext;
    }

    public void getAsyncSpecialEventsList(int eventId, Integer max) {
        if (Utility.isNetworkConnected(this.mContext)) {
            SpecialEventSambaApi specialEventApi = new SpecialEventSambaApi();

            Map<String, String> options = null;

            if (max != null) {
                options = new HashMap<String, String>();

                options.put("max", max.toString());
            }

            specialEventApi.index(eventId, options, new SpecialEventCallback());
        }
        else {
            //No Connection
        }
    }

    private class SpecialEventCallback implements Callback<SambaApi.SambaApiResponse<SpecialEvent>> {

        public SpecialEventCallback() {
            super();
        }

        @Override
        public void success(SambaApi.SambaApiResponse<SpecialEvent> specialEventSambaApiResponse, Response response) {
            if (specialEventSambaApiResponse != null && specialEventSambaApiResponse.meta.status == 200) {
                ArrayList<SpecialEvent> specialEventsList = new ArrayList<SpecialEvent>();

                specialEventsList.addAll(specialEventSambaApiResponse.data);

                SpecialEventBusiness.this.mEventIndexInterface.onIndexReceived(specialEventsList);
            }
            else
            {

            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }
}
