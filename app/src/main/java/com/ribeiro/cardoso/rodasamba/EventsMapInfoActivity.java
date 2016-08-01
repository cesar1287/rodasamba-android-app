package com.ribeiro.cardoso.rodasamba;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.util.ArrayList;


public class EventsMapInfoActivity extends FragmentActivity {
    public static final String EXTRA_LATITUDE = "LATITUDE";
    public static final String EXTRA_LONGITUDE = "LONGITUDE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        final Bundle extras = getIntent().getExtras();

        final WindowManager.LayoutParams params = this.getWindow().getAttributes();


        setContentView(R.layout.activity_events_map_info);


        if (savedInstanceState == null) {
            PlaceholderFragment fragment = PlaceholderFragment.newInstance(extras.getDouble(EXTRA_LATITUDE), extras.getDouble(EXTRA_LONGITUDE));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements EventIndexInterface{

        private static final String LOG_TAG = PlaceholderFragment.class.getSimpleName();
        public static final double RANGE_AMOUNT = 0.000001;
        private String[] whereArgsClause;

        private ListView mEventsListView;
        private EventBusiness mEventBusiness;

        private final String whereClause =
                        "(" + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LATITUDE + " >= (? - " + RANGE_AMOUNT + ") AND "
                                + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LATITUDE + " <= (? + " + RANGE_AMOUNT + ")) AND " +
                        "(" + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LONGITUDE + " >= (? - " + RANGE_AMOUNT + ") AND "
                        + SambaContract.EventEntry.TABLE_NAME + "." + SambaContract.EventEntry.COLUMN_LONGITUDE + " <= (? + " + RANGE_AMOUNT + "))";

        private final String[] columns = new String[] {
                SambaContract.EventEntry._ID,
                SambaContract.EventEntry.COLUMN_THUMBNAIL_URL,
                SambaContract.EventEntry.COLUMN_NAME,
                SambaContract.EventEntry.COLUMN_EVENT_DATE,
                SambaContract.EventEntry.COLUMN_TIME,
                SambaContract.EventEntry.COLUMN_REGION_ID,
                SambaContract.EventEntry.COLUMN_ADDRESS,
                SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME
        };

        public PlaceholderFragment(){
        }

        public static PlaceholderFragment newInstance(Double latitude, Double longitude){
            PlaceholderFragment fragment = new PlaceholderFragment();

            fragment.whereArgsClause = new String[]{latitude.toString(),latitude.toString(), longitude.toString(), longitude.toString()};

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_events_map_info, container, false);


            mEventsListView = (ListView)rootView.findViewById(R.id.events_list);

            this.mEventBusiness = new EventBusiness(this, this.getActivity(), columns, true, whereClause, whereArgsClause);
            mEventBusiness.getAsyncEventsRegionList();

            return rootView;
        }

        @Override
        public void onIndexReceived(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
            Log.d(LOG_TAG, "#EVENTS: " + eventsRegionList.size());
            EventListFragment.EventListAdapter eventListAdapter = new EventListFragment.EventListAdapter(this.getActivity(), R.layout.event_list_item, eventsRegionList);
            mEventsListView.setAdapter(eventListAdapter);

        }

        @Override
        public void onIndexError(int errorType) {

        }
    }
}
