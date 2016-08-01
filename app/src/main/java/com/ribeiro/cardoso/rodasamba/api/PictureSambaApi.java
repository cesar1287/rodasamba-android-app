package com.ribeiro.cardoso.rodasamba.api;


import com.ribeiro.cardoso.rodasamba.data.Entities.Picture;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by vinicius on 30/09/14.
 */
public class PictureSambaApi extends SambaApi<Picture> {

    private PictureSambaApiInteface mPictureSambaApiInteface;

    public PictureSambaApi() {
        super();

        this.mPictureSambaApiInteface = restAdapter.create(PictureSambaApiInteface.class);
    }

    public interface PictureSambaApiInteface {
        @GET("/event/{id}/picture")
        void index(@Path("id") int id, @QueryMap Map<String,String> options, Callback<SambaApiResponse<Picture>> cb);
    }

    @Override
    public void index(int id, Map<String, String> options, Callback<SambaApiResponse<Picture>> cb) {
        this.mPictureSambaApiInteface.index(id, options, cb);
    }
}
