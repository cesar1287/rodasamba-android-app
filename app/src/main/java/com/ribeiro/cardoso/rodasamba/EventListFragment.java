package com.ribeiro.cardoso.rodasamba;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.util.ArrayList;

/**
 * Created by vinicius.ribeiro on 24/11/2014.
 */
public class EventListFragment extends Fragment implements EventIndexInterface {

    private static final String LOG_TAG = EventListFragment.class.getSimpleName();

    private EventBusiness mEventBusiness;
    private EventListAdapter mAdapter;
    private MainActivity.EventFilter mFilter;
    private static String trackedFragmentName = "Lista de Eventos";

    private final String[] mRequestedColumns = new String[] {
            SambaContract.EventEntry._ID,
            SambaContract.EventEntry.COLUMN_THUMBNAIL_URL,
            SambaContract.EventEntry.COLUMN_NAME,
            SambaContract.EventEntry.COLUMN_EVENT_DATE,
            SambaContract.EventEntry.COLUMN_TIME,
            SambaContract.EventEntry.COLUMN_REGION_ID,
            SambaContract.EventEntry.COLUMN_ADDRESS,
            SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME,
            SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID
    };

    private ViewHolder mHolder;

    public EventListFragment() {

    }

    public static EventListFragment newInstance(MainActivity.EventFilter filter) {
        EventListFragment fragment = new EventListFragment();

        fragment.mFilter = filter;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_list, container, false);

        this.mHolder = new ViewHolder(rootView);

        Log.d(LOG_TAG, "onCreateView");

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RotateAnimation loadingAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        loadingAnimation.setDuration(4000);
        loadingAnimation.setRepeatCount(Animation.INFINITE);
        loadingAnimation.setInterpolator(new LinearInterpolator());

        this.getActivity().findViewById(R.id.events_list_loading_image).setAnimation(loadingAnimation);

        this.fetchEvents(true);

        Log.d(LOG_TAG, "onViewCreated");
    }

    private void fetchEvents(boolean onlyLocal) {
        this.mHolder.mListLoadingView.setVisibility(View.VISIBLE);
        this.mHolder.mListResultView.setVisibility(View.GONE);

        this.mEventBusiness = new EventBusiness(this, this.getActivity(), this.mRequestedColumns, onlyLocal);

        this.mEventBusiness.getAsyncEventsRegionList();
    }

    public void refreshEvents() {
        if (Utility.isNetworkConnected(this.getActivity())) {
            this.fetchEvents(false);
        }
        else {
            this.startNotConnectedActivity();
        }
    }

    private void startNotConnectedActivity() {
        Intent intent = new Intent(this.getActivity(), NotConnectedActivity.class);

        this.startActivityForResult(intent, NotConnectedActivity.NOT_CONNECTED_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NotConnectedActivity.NOT_CONNECTED_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            }
            else {
                this.getActivity().finish();
            }
        }
    }

    @Override
    public void onIndexReceived(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
        this.mHolder.mListLoadingView.setVisibility(View.GONE);
        this.mHolder.mListResultView.setVisibility(View.VISIBLE);

        this.mAdapter = new EventListAdapter(this.getActivity(), R.layout.event_list_item, eventsRegionList);

        this.mHolder.mEventsListView.setAdapter(this.mAdapter);

        this.mAdapter.setFilter(this.mFilter);
    }

    @Override
    public void onIndexError(int errorType) {

    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mEventBusiness != null) {
            this.mEventBusiness.unregisterReceivers();
        }

        Log.d(LOG_TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.mEventBusiness != null) {
            this.mEventBusiness.unregisterReceivers();
        }

        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();

        SplashScreenActivity.tracker.setScreenName(EventListFragment.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void setFilter(MainActivity.EventFilter filter) {
        this.mFilter = filter;
        this.mAdapter.setFilter(this.mFilter);
    }

    public static class EventListAdapter extends ArrayAdapter<Utility.Pair<Event,Region>> {

        private ArrayList<Utility.Pair<Event,Region>> mFilteredEventsRegionList;

        private final ArrayList<Utility.Pair<Event,Region>> mCompleteEventsRegionList;
        private final int mLayout;
        private final Context mContext;

        public EventListAdapter(Context context, int layout, ArrayList<Utility.Pair<Event,Region>> eventsRegionList) {
            super(context, layout, eventsRegionList);

            this.mContext = context;
            this.mCompleteEventsRegionList = (ArrayList<Utility.Pair<Event,Region>>) eventsRegionList.clone();
            this.mFilteredEventsRegionList = (ArrayList<Utility.Pair<Event,Region>>) eventsRegionList.clone();
            this.mLayout = layout;
        }

        @Override
        public int getCount() {
            return this.mFilteredEventsRegionList.size();
        }

        @Override
        public View getView(int position, View contentView, ViewGroup parent) {

            View view;
            ViewHolder holder = null;

            if (contentView == null) {

                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(mLayout, null);

                if (view != null) {
                    holder = new ViewHolder(view);
                    view.setTag(holder);
                }
            }
            else {
                view = contentView;
                holder = (ViewHolder) contentView.getTag();
            }

            if (holder != null) {
                final Utility.Pair<Event,Region> eventRegionPair = this.mFilteredEventsRegionList.get(position);

                if (eventRegionPair != null) {
                    final Event event = eventRegionPair.getLeft();

                    holder.eventItemName.setText(event.name);
                    holder.eventItemName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    holder.eventItemName.setSingleLine(true);
                    holder.eventItemName.setMarqueeRepeatLimit(1);
                    holder.eventItemName.setSelected(true);

                    holder.eventItemDay.setText(Utility.getDayFromDateString(event.date));
                    holder.eventItemMonth.setText(Utility.getMonthFromDateString(event.date));
                    holder.eventItemHour.setText(Utility.getHourFromTimeString(event.time));
                    holder.eventItemMinute.setText(":" + Utility.getMinuteFromTimeString(event.time));

                    Region region = eventRegionPair.getRight();

                    holder.eventItemRegion.setText(region.name);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent eventShowIntent = new Intent(mContext, EventShowActivity.class);
                            eventShowIntent.putExtra(EventShowActivity.EVENT_ID_MESSAGE, event.id);

                            mContext.startActivity(eventShowIntent);
                        }
                    });
                }
            }

            return view;
        }

        public void setFilter(MainActivity.EventFilter filter) {
            this.mFilteredEventsRegionList.clear();

            for (Utility.Pair<Event, Region> eventRegionPair : this.mCompleteEventsRegionList) {
                Event event = eventRegionPair.getLeft();


                if (filter == null || (filter != null && filter.getThrough(event))) {
                    this.mFilteredEventsRegionList.add(eventRegionPair);
                }
            }

            this.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            TextView searchEmptyView = (TextView)((Activity)this.mContext).findViewById(R.id.search_empty_view);
            ListView eventsList = (ListView)((Activity)this.mContext).findViewById(R.id.events_list);

            if (this.mFilteredEventsRegionList.size() == 0) {
                searchEmptyView.setVisibility(View.VISIBLE);
                eventsList.setVisibility(View.GONE);
            }
            else {
                searchEmptyView.setVisibility(View.GONE);
                eventsList.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Cache of the children views for a forecast list item.
         */
        public static class ViewHolder {
            public final TextView eventItemName;
            public final TextView eventItemRegion;
            public final TextView eventItemDay;
            public final TextView eventItemMonth;
            public final TextView eventItemHour;
            public final TextView eventItemMinute;

            public ViewHolder(View view) {
                eventItemName = (TextView) view.findViewById(R.id.eventItemName);
                eventItemRegion = (TextView) view.findViewById(R.id.eventItemRegion);
                eventItemDay = (TextView) view.findViewById(R.id.eventItemDay);
                eventItemMonth = (TextView) view.findViewById(R.id.eventItemMonth);
                eventItemHour = (TextView) view.findViewById(R.id.eventItemHour);
                eventItemMinute = (TextView) view.findViewById(R.id.eventItemMinute);
            }
        }
    }

    private static class ViewHolder {
        public RelativeLayout mListLoadingView;
        public LinearLayout mListResultView;
        public TextView mEmptySearchView;

        public ListView mEventsListView;

        public ViewHolder(View view) {
            this.mListLoadingView = (RelativeLayout) view.findViewById(R.id.events_list_loading_view);
            this.mListResultView = (LinearLayout) view.findViewById(R.id.event_lists_result_view);
            this.mEmptySearchView = (TextView) view.findViewById(R.id.search_empty_view);

            this.mEventsListView = (ListView) view.findViewById(R.id.events_list);
        }
    }
}
