package com.ribeiro.cardoso.rodasamba.api;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;

/**
 * Created by diegopc86 on 16/08/14.
 */
public abstract class SambaApi <T>{

    //public final static String BASE_URL = "http://samba.vlribeiro.com.br/"; //dev
    public final static String BASE_URL = "http://venturafilmes.com.br/samba/public/"; //prod
    private final static String API_URL = BASE_URL + "api/1.0/";


    protected RestAdapter restAdapter;

    public SambaApi(){
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();
    }

    public void show(int id, Callback<SambaApiResponse<T>> cb) { throw new UnsupportedOperationException(); }
    public void index(Callback<SambaApiResponse<T>> cb) { throw new UnsupportedOperationException(); }
    public void index(Map<String,String> options, Callback<SambaApiResponse<T>> cb) { throw new UnsupportedOperationException(); }
    public void index(int id, Map<String,String> options, Callback<SambaApiResponse<T>> cb) { throw new UnsupportedOperationException(); }

    public static class SambaApiResponse <R>{

        public int length;
        public List<R> data;
        public MetaResponse meta;

        public static class MetaResponse {
            public int status;
            public String message;
        };

    }

}
