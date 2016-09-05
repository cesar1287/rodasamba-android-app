package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.User;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by diegopc86 on 16/08/14.
 */
public class UserSambaApi extends SambaApi<User> {

    private final static String LOG_TAG = UserSambaApi.class.getSimpleName();

    private UserSambaApiInteface userSambaApiInteface;

    public UserSambaApi() {
        super();

        this.userSambaApiInteface = restAdapter.create(UserSambaApiInteface.class);
    }


    public interface UserSambaApiInteface {

        @GET("/user/{id}")
        void show(@Path("id") String id, Callback<SambaApiResponse<User>> cb);

        @FormUrlEncoded
        @POST("/user")
        void post(@Field("sex") String sex, @Field("region_id") int region_id, @Field("age_group_id") int age_group_id, @Field("device_os") String device_os, @Field("device_name") String device_name, Callback<SambaApiResponse<User>> cb );

        @FormUrlEncoded
        @PUT("/user/{id}")
        void put(@Path("id") String id, @Field("sex") String sex, @Field("region_id") int region_id, @Field("age_group_id") int age_group_id, @Field("device_os") String device_os, @Field("device_name") String device_name, Callback<SambaApiResponse<User>> cb );

    }

    @Override
    public void show(int id, Callback<SambaApiResponse<User>> cb) {
    }


    @Override
    public void index(Callback<SambaApiResponse<User>> cb) {
    }

    @Override
    public void index(Map<String, String> options, Callback<SambaApiResponse<User>> cb) {

    }

    public void post(User user, Callback<SambaApiResponse<User>> cb){
        this.userSambaApiInteface.post(user.sex, user.region_id, user.age_group_id, user.device_os, user.device_name, cb);
    }

    public void put(User user, Callback<SambaApiResponse<User>> cb){
        this.userSambaApiInteface.put(user.id, user.sex, user.region_id, user.age_group_id, user.device_os, user.device_name, cb);
    }
}
