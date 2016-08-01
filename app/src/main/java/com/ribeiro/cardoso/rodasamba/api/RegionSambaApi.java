package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.Region;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by diegopc86 on 16/08/14.
 */
public class RegionSambaApi extends SambaApi<Region> {

    private final static String LOG_TAG = RegionSambaApi.class.getSimpleName();

    private RegionSambaApiInteface regionSambaApiInteface;

    public RegionSambaApi() {
        super();

        this.regionSambaApiInteface = restAdapter.create(RegionSambaApiInteface.class);
    }


    public interface RegionSambaApiInteface{

        @GET("/region")
        void index(Callback<SambaApiResponse<Region>> cb);

        @GET("/region/{id}")
        void show(@Path("id") int id, Callback<SambaApiResponse<Region>> cb);

    }

    @Override
    public void show(int id, Callback<SambaApiResponse<Region>> cb) {
    }

    @Override
    public void index(Map<String, String> options, Callback<SambaApiResponse<Region>> cb) {

    }

    @Override
    public void index(Callback<SambaApiResponse<Region>> cb) {
        this.regionSambaApiInteface.index(cb);
    }
}
