package com.ribeiro.cardoso.rodasamba.business;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.service.SyncedEventDatabaseService;
import com.ribeiro.cardoso.rodasamba.tasks.FetchEventsTask;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 01/09/2014.
 */
public class EventBusiness implements FetchEventsTask.FetchEventsTaskReceiver {

    private final EventIndexInterface mEventIndexInterface;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final EventShowInterface mEventShowInterface;
    private final Context mContext;
    private final boolean mOnlyLocal;
    private final String[] mRequestedColumns;

    private SyncedEventReceiver mReceiver;
    private Integer mEventId;

    public EventBusiness(EventIndexInterface eventInterface, Context context, String[] columns, boolean onlyLocal) {
        this.mEventIndexInterface = eventInterface;
        this.mEventShowInterface = null;
        this.mContext = context;
        this.mRequestedColumns = columns;
        this.mOnlyLocal = onlyLocal;
        this.mSelection = null;
        this.mSelectionArgs = null;
    }

    public EventBusiness(EventIndexInterface eventInterface, Context context, String[] columns, boolean onlyLocal, String selection, String[] selectionArgs) {
        this.mEventIndexInterface = eventInterface;
        this.mEventShowInterface = null;
        this.mContext = context;
        this.mRequestedColumns = columns;
        this.mOnlyLocal = onlyLocal;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
    }

    public EventBusiness(EventShowInterface eventInterface, Context context, String[] columns, boolean onlyLocal) {
        this.mEventIndexInterface = null;
        this.mEventShowInterface = eventInterface;
        this.mContext = context;
        this.mRequestedColumns = columns;
        this.mOnlyLocal = onlyLocal;
        this.mSelection = null;
        this.mSelectionArgs = null;
    }

    public void getAsyncEventsRegionList() {
        this.mEventId = null;

        if (!this.mOnlyLocal) {
            if (Utility.isNetworkConnected(this.mContext)) {

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SyncedEventDatabaseService.SERVICE_STATUS);

                mReceiver = new SyncedEventReceiver();
                mContext.registerReceiver(mReceiver, intentFilter);

                Intent serviceIntent = new Intent(this.mContext, SyncedEventDatabaseService.class);
                serviceIntent.putExtra(SyncedEventDatabaseService.REQUESTED_COLUMNS_ARRAY, this.mRequestedColumns);

                this.mContext.startService(serviceIntent);
            }
            else {
                //No Connection
            }
        }
        else {
            this.executeDbFetch();
        }
    }

    public void getAsyncEvent(int event_id) {
        this.mEventId = event_id;

        if (!this.mOnlyLocal) {
            if (Utility.isNetworkConnected(this.mContext)) {

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SyncedEventDatabaseService.SERVICE_STATUS);

                mReceiver = new SyncedEventReceiver();
                mContext.registerReceiver(mReceiver, intentFilter);

                Intent serviceIntent = new Intent(this.mContext, SyncedEventDatabaseService.class);
                serviceIntent.putExtra(SyncedEventDatabaseService.REQUESTED_EVENT_ID, event_id);

                this.mContext.startService(serviceIntent);
            }
            else {
                //No connection
            }
        }
        else {
            this.executeDbFetch();
        }
    }

    private void executeDbFetch() {
        FetchEventsTask dbFetch = new FetchEventsTask(this, this.mContext, this.mRequestedColumns, this.mEventId, this.mSelection, this.mSelectionArgs);
        dbFetch.execute();
    }

    public void unregisterReceivers() {
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    @Override
    public void onReceiveEvents(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
        if (this.mEventIndexInterface != null) {
            this.mEventIndexInterface.onIndexReceived(eventsRegionList);
        }
        if (this.mEventShowInterface != null) {
            if (eventsRegionList.size() > 0) {
                this.mEventShowInterface.onShowReceived(eventsRegionList.get(0));
            }
            else {
                this.mEventShowInterface.onShowReceived(null);
            }
        }
    }

    @Override
    public void onGetEventsError(int errorReason) {
        if (this.mEventIndexInterface != null) {
            this.mEventIndexInterface.onIndexError(errorReason);
        }
        if (this.mEventShowInterface != null) {
            this.mEventShowInterface.onShowError(errorReason);
        }
    }

    private class SyncedEventReceiver extends BroadcastReceiver {

        public SyncedEventReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getIntExtra(SyncedEventDatabaseService.STATUS, SyncedEventDatabaseService.SERVICE_FAIL) == SyncedEventDatabaseService.SERVICE_SUCCESS) {
                EventBusiness.this.executeDbFetch();
            }
        }
    }

}
