package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.Sex;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by diegopc86 on 23/08/14.
 */
public class SexSambaApi extends SambaApi<Sex>  {

    private final SexSambaInterface sexSambaInterface;

    public SexSambaApi() {
        super();

        sexSambaInterface = this.restAdapter.create(SexSambaInterface.class);
    }

    private interface SexSambaInterface{
        @GET("/sex")
        void index(Callback<SambaApiResponse<Sex>> cb);
    }

    @Override
    public void show(int id, Callback<SambaApiResponse<Sex>> cb) {

    }

    @Override
    public void index(Map<String, String> options, Callback<SambaApiResponse<Sex>> cb) {

    }

    @Override
    public void index(Callback<SambaApiResponse<Sex>> cb) {
        this.sexSambaInterface.index(cb);
    }
}
