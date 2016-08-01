package com.ribeiro.cardoso.rodasamba.service;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.api.EventSambaApi;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vinicius.ribeiro on 27/08/2014.
 */
public class SyncedEventDatabaseService extends IntentService {

    private final static String LOG_TAG = SyncedEventDatabaseService.class.getSimpleName();

    private EventSambaApi mEventApi;
    private SambaDbHelper mSambaHelper;
    private String mRequestedColumns;

    public static final String SERVICE_STATUS = "service_status";
    public static final String STATUS = "status";
    public static final int SERVICE_FAIL = 0;
    public static final int SERVICE_SUCCESS = 1;
    public static final String REQUESTED_EVENT_ID = "requested_event_id";
    public static final String REQUESTED_COLUMNS_ARRAY = "requested_columns_array";

    public SyncedEventDatabaseService() {
        super("SyncedEventDatabaseService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.mSambaHelper = SambaDbHelper.getInstance(this);
        this.mEventApi = new EventSambaApi();

        Map<String, String> options = new HashMap<String, String>();

        Bundle extras = intent.getExtras();

        if (extras != null && extras.get(SyncedEventDatabaseService.REQUESTED_EVENT_ID) != null) {
            int requestedEventId = extras.getInt(SyncedEventDatabaseService.REQUESTED_EVENT_ID);

            this.mEventApi.show(requestedEventId,new EventCallbackHandler(this, this.mSambaHelper, false));
        }
        else {
            if (extras != null && extras.get(SyncedEventDatabaseService.REQUESTED_COLUMNS_ARRAY) != null) {
                this.mRequestedColumns = this.getRequestedColumnsFromColumnsArray((String[]) extras.get(SyncedEventDatabaseService.REQUESTED_COLUMNS_ARRAY));

                options.put("fields", this.mRequestedColumns);
            }

            this.mEventApi.index(options, new EventCallbackHandler(this, this.mSambaHelper, true));
        }
    }

    private String getRequestedColumnsFromColumnsArray(String[] columns) {

        if (columns == null || columns.length == 0) {
            return null;
        }

        ArrayList<String> allowedColumnsToGet = getAllowedColumnsToGet(columns);

        StringBuilder sb = new StringBuilder();

        for (String column : allowedColumnsToGet) {
            sb.append(column).append(",");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private ArrayList<String> getAllowedColumnsToGet(String[] columns) {
        ArrayList<String> allowedColumnsToGet = new ArrayList<String>();

        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_NAME);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_ADDRESS);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_DESCRIPTION);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_DRINK_PRICE);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_EVENT_DATE);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_REGION_ID);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_TICKET_PRICE);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_TIME);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_URL);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_LATITUDE);
        allowedColumnsToGet.add(SambaContract.EventEntry.COLUMN_LONGITUDE);

        ArrayList<String> allowedRequestedColumns = new ArrayList<String>();

        for (String column : columns) {
            if (allowedColumnsToGet.contains(column)) {
                allowedRequestedColumns.add(column);
            }
            else if (column.contentEquals(SambaContract.EventEntry._ID)) {
                allowedRequestedColumns.add("id");
            }
        }

        return allowedRequestedColumns;
    }

    private static class EventCallbackHandler implements Callback<SambaApi.SambaApiResponse<Event>> {
        private final SyncedEventDatabaseService mContext;
        private final SambaDbHelper mSambaHelper;
        private final Geocoder mGeocoder;
        private final boolean deleteAll;


        public EventCallbackHandler(SyncedEventDatabaseService mContext, SambaDbHelper mSambaDbHelper, boolean deleteAll) {
            this.mContext = mContext;
            this.mSambaHelper = mSambaDbHelper;
            mGeocoder = new Geocoder(mContext);
            this.deleteAll = deleteAll;

        }

        @Override
        public void success(SambaApi.SambaApiResponse<Event> eventSambaApiResponse, Response response) {
            if (eventSambaApiResponse != null && eventSambaApiResponse.data.size() > 0) {
                //update lastSync on the shared prefs


                SQLiteDatabase db = mSambaHelper.getWritableDatabase();

                List<Event> events = eventSambaApiResponse.data;

                DbTask dbTask = new DbTask(db, this);

                dbTask.execute(events.toArray(new Event[events.size()]));
            }
        }

        public ContentValues getContentValues(Event event) throws IOException, Exception {
            ContentValues cv = new ContentValues(10);

            final int INDEX_STREET_ADDRESS = 0;
            final int INDEX_CITY_ADRESS = 1;

            Address addressLocated = null;

            cv.put(SambaContract.EventEntry._ID, event.id);
            cv.put(SambaContract.EventEntry.COLUMN_NAME, event.name);
            cv.put(SambaContract.EventEntry.COLUMN_ADDRESS, event.address);
            cv.put(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL, event.thumbnail_url);
            cv.put(SambaContract.EventEntry.COLUMN_REGION_ID, event.region_id);
            cv.put(SambaContract.EventEntry.COLUMN_EVENT_DATE, event.date);
            cv.put(SambaContract.EventEntry.COLUMN_TIME, event.time);
            cv.put(SambaContract.EventEntry.COLUMN_DESCRIPTION, event.description);
            cv.put(SambaContract.EventEntry.COLUMN_TICKET_PRICE, event.ticket_price);
            cv.put(SambaContract.EventEntry.COLUMN_DRINK_PRICE, event.drink_price);
            cv.put(SambaContract.EventEntry.COLUMN_LATITUDE, event.latitude);
            cv.put(SambaContract.EventEntry.COLUMN_LONGITUDE, event.longitude);

//            try {
//                List<Address> fromLocationName = mGeocoder.getFromLocationName(event.address, 1);
//
//                if (fromLocationName.size() > 0) {
//                    addressLocated = fromLocationName.get(0);
//                    Log.d(LOG_TAG, "Geocoded: " + event.name);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw e;
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw e;
//            }
//
//            if (null != addressLocated) {
//                cv.put(SambaContract.EventEntry.COLUMN_LATITUDE, addressLocated.getLatitude());
//                cv.put(SambaContract.EventEntry.COLUMN_LONGITUDE, addressLocated.getLongitude());
//
//                cv.remove(SambaContract.EventEntry.COLUMN_ADDRESS);
//                String address = String.format("%s, %s", addressLocated.getAddressLine(INDEX_STREET_ADDRESS), addressLocated.getAddressLine(INDEX_CITY_ADRESS));
//                cv.put(SambaContract.EventEntry.COLUMN_ADDRESS, address);
//            }

            return cv;
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Intent intent = new Intent();
            intent.setAction(SyncedEventDatabaseService.SERVICE_STATUS);
            intent.putExtra(SyncedEventDatabaseService.STATUS, SyncedEventDatabaseService.SERVICE_FAIL);

            mContext.sendBroadcast(intent);
        }

        private class DbTask extends AsyncTask<Event, Void, Void> {
            private SQLiteDatabase mDB;
            private EventCallbackHandler mCallbackHandler;

            public DbTask (SQLiteDatabase db, EventCallbackHandler handler) {
                this.mDB = db;
                this.mCallbackHandler = handler;
            }

            @Override
            protected Void doInBackground(Event... events) {
                Log.d(LOG_TAG, "Iniciando carga...");

                try {
                    this.mDB.beginTransaction();

                    if (this.mCallbackHandler.deleteAll){
                        this.mDB.delete(SambaContract.EventEntry.TABLE_NAME, null, null);
                    }

                    for (Event event : events) {
                        ContentValues cv = this.mCallbackHandler.getContentValues(event);

                        this.mDB.insert(SambaContract.EventEntry.TABLE_NAME, null, cv);
                    }

                    this.mDB.setTransactionSuccessful();
                }
                catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                finally {
                    this.mDB.endTransaction();

                    return null;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Utility.updateLastSync(this.mCallbackHandler.mContext);

                Log.d(LOG_TAG, "Carga finalizada!");

                Intent intent = new Intent();
                intent.setAction(SyncedEventDatabaseService.SERVICE_STATUS);
                intent.putExtra(SyncedEventDatabaseService.STATUS, SyncedEventDatabaseService.SERVICE_SUCCESS);

                mContext.sendBroadcast(intent);
            }
        }
    }
}
