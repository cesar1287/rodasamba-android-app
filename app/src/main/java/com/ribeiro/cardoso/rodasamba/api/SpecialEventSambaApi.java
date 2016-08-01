package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.SpecialEvent;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by vinicius on 03/09/14.
 */
public class SpecialEventSambaApi extends SambaApi<SpecialEvent> {

    private SpecialEventSambaApiInteface mSpecialEventSambaApiInteface;

    public SpecialEventSambaApi() {
        super();

        this.mSpecialEventSambaApiInteface = restAdapter.create(SpecialEventSambaApiInteface.class);
    }

    public interface SpecialEventSambaApiInteface {
        @GET("/event/{id}/calendar")
        void index(@Path("id") int id, @QueryMap Map<String,String> options, Callback<SambaApiResponse<SpecialEvent>> cb);
    }

    @Override
    public void index(int id, Map<String, String> options, Callback<SambaApiResponse<SpecialEvent>> cb) {
        this.mSpecialEventSambaApiInteface.index(id, options, cb);
    }
}
