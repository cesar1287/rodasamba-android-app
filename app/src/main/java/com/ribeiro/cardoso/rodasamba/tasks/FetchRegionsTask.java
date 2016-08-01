package com.ribeiro.cardoso.rodasamba.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;

import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 02/12/2014.
 */
public class FetchRegionsTask extends AsyncTask<Void, Void, ArrayList<Region>> {
    public static final int FETCH_SUCESSFUL = 0;

    private final FetchRegionsTaskReceiver mReceiver;
    private final Context mContext;

    private int mFetchStatus = 0;

    public FetchRegionsTask(FetchRegionsTaskReceiver receiver, Context context) {
        this.mReceiver = receiver;
        this.mContext = context;
    }

    @Override
    protected ArrayList<Region> doInBackground(Void... voids) {
        SQLiteDatabase db = SambaDbHelper.getInstance(this.mContext).getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SambaContract.RegionEntry.TABLE_NAME);

        String sortOrder = SambaContract.RegionEntry.TABLE_NAME + "." + SambaContract.RegionEntry.COLUMN_NAME;

        return this.getRegionsListFromCursor(qb.query(db, null, null, null, null, null, sortOrder));
    }

    @Override
    protected void onPostExecute(ArrayList<Region> regionsList) {
        super.onPostExecute(regionsList);

        switch (this.mFetchStatus) {
            case FetchEventsTask.FETCH_SUCESSFUL:
                mReceiver.onReceiveRegions(regionsList);
                break;
            default:
                mReceiver.onGetRegionsError(this.mFetchStatus);
                break;
        }
    }

    private ArrayList<Region> getRegionsListFromCursor(Cursor cursor) {
        ArrayList<Region> regionsList = new ArrayList<Region>();

        while (cursor.moveToNext()) {
            Region region = new Region();

            region.name = cursor.getString(cursor.getColumnIndex(SambaContract.RegionEntry.COLUMN_NAME));
            region.id = cursor.getInt(cursor.getColumnIndex(SambaContract.RegionEntry._ID));

            regionsList.add(region);
        }

        return regionsList;
    }

    public static interface FetchRegionsTaskReceiver {
        public void onReceiveRegions(ArrayList<Region> cursor);

        public void onGetRegionsError(int errorReason);
    }
}
