package com.ribeiro.cardoso.rodasamba.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;

import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;
import com.ribeiro.cardoso.rodasamba.util.Utility.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinicius.ribeiro on 29/08/2014.
 */
public class FetchEventsTask extends AsyncTask<Void, Void, ArrayList<Pair<Event,Region>>> {

    public static final int FETCH_SUCESSFUL = 0;

    private final FetchEventsTaskReceiver mReceiver;
    private final Context mContext;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final HashMap<String,String> mProjection;
    private final Integer mEventId;

    private int mFetchStatus = 0;

    public FetchEventsTask(FetchEventsTaskReceiver receiver, Context context, String[] columns, Integer eventId) {
        this.mReceiver = receiver;
        this.mContext = context;
        this.mProjection = this.createProjectionHashMapFromColumns(columns);
        this.mEventId = eventId;
        this.mSelection = null;
        this.mSelectionArgs = null;
    }

    public FetchEventsTask(FetchEventsTaskReceiver receiver, Context context, String[] columns, Integer eventId, String selection, String[] selectionArgs) {
        this.mReceiver = receiver;
        this.mContext = context;
        this.mProjection = this.createProjectionHashMapFromColumns(columns);
        this.mEventId = eventId;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
    }

    @Override
    protected ArrayList<Pair<Event,Region>> doInBackground(Void... voids) {
        SQLiteDatabase db = SambaDbHelper.getInstance(this.mContext).getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SambaContract.EventEntry.TABLE_NAME + " INNER JOIN " + SambaContract.RegionEntry.TABLE_NAME +
                " ON " + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_REGION_ID + " = " + SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry._ID);

        if (this.mProjection != null) {
            qb.setProjectionMap(this.mProjection);
        }

        String sortOrder = SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_EVENT_DATE + ", " + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_TIME;

        return this.getEventsRegionListFromCursor(qb.query(db, null, this.getWhereClause(), this.getWhereArgsClause(), null, null, sortOrder));
    }

    @Override
    protected void onPostExecute(ArrayList<Pair<Event,Region>> eventsRegionList) {
        super.onPostExecute(eventsRegionList);

        switch (this.mFetchStatus) {
            case FetchEventsTask.FETCH_SUCESSFUL:
                mReceiver.onReceiveEvents(eventsRegionList);
                break;
            default:
                mReceiver.onGetEventsError(this.mFetchStatus);
                break;
        }
    }

    private HashMap<String,String> createProjectionHashMapFromColumns(String[] columns) {
        HashMap<String,String> projectionHashMap = new HashMap<String, String>();

        for (String column : columns) {
            //Java does not allow switch with strings, there's nothing i can do to keep it prettier.
            if (column.contentEquals(SambaContract.EventEntry._ID)) {
                projectionHashMap.put(SambaContract.EventEntry._ID, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry._ID);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_NAME)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_NAME, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_NAME);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_EVENT_DATE)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_EVENT_DATE, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_EVENT_DATE);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_TIME)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_TIME, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_TIME);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_ADDRESS)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_ADDRESS, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_ADDRESS);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_DESCRIPTION)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_DESCRIPTION, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_DESCRIPTION);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_TICKET_PRICE)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_TICKET_PRICE, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_TICKET_PRICE);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_DRINK_PRICE)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_DRINK_PRICE, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_DRINK_PRICE);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_THUMBNAIL_URL);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_LATITUDE)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_LATITUDE, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LATITUDE);
            }
            if (column.contentEquals(SambaContract.EventEntry.COLUMN_LONGITUDE)) {
                projectionHashMap.put(SambaContract.EventEntry.COLUMN_LONGITUDE, SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LONGITUDE);
            }
            if (column.contentEquals(SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME)) {
                projectionHashMap.put(SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry.COLUMN_NAME, SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry.COLUMN_NAME + " AS " + SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME);
            }
            if (column.contentEquals(SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID)) {
                projectionHashMap.put(SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry._ID, SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry._ID + " AS " + SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID);
            }
        }

        return projectionHashMap;
    }

    private String getWhereClause() {
        String whereClause = null;

        if (this.mEventId != null) {
            whereClause = SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry._ID + " = ?";
        }else{
            whereClause = this.mSelection;
        }

        return whereClause;
    }

    private String[] getWhereArgsClause(){
        String[] whereArgsClause;

        if (this.mEventId != null){
            whereArgsClause = new String[]{this.mEventId.toString()};
        }else{
            whereArgsClause = this.mSelectionArgs;
        }

        return  whereArgsClause;
    }

    private ArrayList<Pair<Event,Region>> getEventsRegionListFromCursor(Cursor cursor) {
        ArrayList<Pair<Event,Region>> eventsRegionList = new ArrayList<Pair<Event, Region>>();

        while (cursor.moveToNext()) {
            Event event = new Event();
            Region region = new Region();

            if (mProjection.get(SambaContract.EventEntry._ID) != null) {
                event.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry._ID)));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_NAME) != null) {
                event.name = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_NAME));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_EVENT_DATE) != null) {
                event.date = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_EVENT_DATE));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_TIME) != null) {
                event.time = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_TIME));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_ADDRESS) != null) {
                event.address = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_ADDRESS));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_DESCRIPTION) != null) {
                event.description = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_DESCRIPTION));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_TICKET_PRICE) != null) {
                event.ticket_price = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_TICKET_PRICE));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_DRINK_PRICE) != null) {
                event.drink_price = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_DRINK_PRICE));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL) != null) {
                event.thumbnail_url = cursor.getString(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_THUMBNAIL_URL));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_LATITUDE) != null) {
                event.latitude = cursor.getDouble(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_LATITUDE));
            }
            if (mProjection.get(SambaContract.EventEntry.COLUMN_LONGITUDE) != null) {
                event.longitude = cursor.getDouble(cursor.getColumnIndex(SambaContract.EventEntry.COLUMN_LONGITUDE));
            }
            if (mProjection.get(SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry.COLUMN_NAME) != null) {
                region.name = cursor.getString(cursor.getColumnIndex(SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME));
            }
            if (mProjection.get(SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry._ID) != null) {
                final int region_id = cursor.getInt(cursor.getColumnIndex(SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID));
                region.id = region_id;
                event.region_id = region_id;

            }

            eventsRegionList.add(new Pair<Event,Region>(event, region));
        }

        return eventsRegionList;
    }

    public static interface FetchEventsTaskReceiver {
        public void onReceiveEvents(ArrayList<Pair<Event, Region>> cursor);

        public void onGetEventsError(int errorReason);
    }
}
