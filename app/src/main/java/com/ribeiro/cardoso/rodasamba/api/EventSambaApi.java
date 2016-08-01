package com.ribeiro.cardoso.rodasamba.api;

import com.ribeiro.cardoso.rodasamba.data.Entities.Event;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by diegopc86 on 16/08/14.
 */
public class EventSambaApi extends SambaApi<Event> {

    private final static String LOG_TAG = EventSambaApi.class.getSimpleName();

    private EventSambaApiInteface eventSambaApiInteface;

    public EventSambaApi() {
        super();

        this.eventSambaApiInteface = restAdapter.create(EventSambaApiInteface.class);
    }


    public interface EventSambaApiInteface {

        @GET("/event")
        void index(@QueryMap Map<String,String> options, Callback<SambaApiResponse<Event>> cb);

        @GET("/event")
        void index(Callback<SambaApiResponse<Event>> cb);

        @GET("/event/{id}")
        void show(@Path("id") int id, Callback<SambaApiResponse<Event>> cb);

    }

    @Override
    public void show(int id, Callback<SambaApiResponse<Event>> cb) {
        this.eventSambaApiInteface.show(id, cb);
    }

    @Override
    public void index(Callback<SambaApiResponse<Event>> cb) {
        this.eventSambaApiInteface.index(cb);
    }

    @Override
    public void index(Map<String,String> options, Callback<SambaApiResponse<Event>> cb) {
        if (options == null || options.size() == 0) {
            this.eventSambaApiInteface.index(cb);
        }
        else {
            this.eventSambaApiInteface.index(options, cb);
        }
    }
}
