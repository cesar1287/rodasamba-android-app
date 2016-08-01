package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.AgeGroup;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by diegopc86 on 19/08/14.
 */
public class AgeGroupSambaApi extends SambaApi<AgeGroup> {

    private AgeGroupSambaApiInterface ageGroupSambaApiInterface;


    public AgeGroupSambaApi() {
        super();

        ageGroupSambaApiInterface = this.restAdapter.create(AgeGroupSambaApiInterface.class);
    }

    private interface AgeGroupSambaApiInterface{

        @GET("/age-group")
        void index(Callback<SambaApiResponse<AgeGroup>> cb);

        @GET("/age-group/{id}")
        void index(@Path("id") int id, Callback<SambaApiResponse<AgeGroup>> cb);
    }


    @Override
    public void show(int id, Callback<SambaApiResponse<AgeGroup>> cb) {
        this.ageGroupSambaApiInterface.index(id, cb);
    }

    @Override
    public void index(Map<String, String> options, Callback<SambaApiResponse<AgeGroup>> cb) {

    }

    @Override
    public void index(Callback<SambaApiResponse<AgeGroup>> cb) {
        this.ageGroupSambaApiInterface.index(cb);
    }
}
