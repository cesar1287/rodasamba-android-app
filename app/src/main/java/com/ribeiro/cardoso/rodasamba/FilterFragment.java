package com.ribeiro.cardoso.rodasamba;


import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ribeiro.cardoso.rodasamba.business.RegionBusiness;
import com.ribeiro.cardoso.rodasamba.business.RegionIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;

import java.util.ArrayList;

public class FilterFragment extends Fragment implements RegionIndexInterface {

    private EventFilterListener mFilterListener;
    private ViewHolder mHolder;
    private MainActivity.EventFilter mFilter;

    public static FilterFragment newInstance(MainActivity.EventFilter filter) {
        FilterFragment fragment = new FilterFragment();

        fragment.mFilter = filter;

        return fragment;
    }

    public FilterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);

        //Gets the region info
        RegionBusiness regionBusiness = new RegionBusiness(this, this.getActivity());
        regionBusiness.getAsyncRegionsList();

        this.mHolder = new ViewHolder(rootView);

        //Date switchs
        DateSwitchChangeListener dateFilterChangeListener = new DateSwitchChangeListener(this.mHolder);

        this.mHolder.mTodayFilter.setOnCheckedChangeListener(dateFilterChangeListener);
        this.mHolder.mThisWeekFilter.setOnCheckedChangeListener(dateFilterChangeListener);
        this.mHolder.mThisMonthFilter.setOnCheckedChangeListener(dateFilterChangeListener);

        //Deals with button clicks
        FilterActionsClickListener actionsListener = new FilterActionsClickListener(this.mHolder, this.mFilterListener);

        this.mHolder.mApplyFilter.setOnClickListener(actionsListener);
        this.mHolder.mClearFilter.setOnClickListener(actionsListener);

        if (this.mFilter != null) {
            this.mHolder.mEventFilter.setText(this.mFilter.getName());

            switch (this.mFilter.getDate()) {
                case TODAY_FILTER:
                    this.mHolder.mTodayFilter.setChecked(true);
                    break;
                case THIS_WEEK_FILTER:
                    this.mHolder.mThisWeekFilter.setChecked(true);
                    break;
                case THIS_MONTH_FILTER:
                    this.mHolder.mThisMonthFilter.setChecked(true);
                    break;
            }
        }

        return rootView;
    }

    @Override
    public void onStop() {
        InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.mHolder.mEventFilter.getWindowToken(), 0);

        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Containing Activity must implement EventFilterListener
        try {
            this.mFilterListener = (EventFilterListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass().getName() + " must implement " + EventFilterListener.class.getName());
        }
    }

    @Override
    public void onIndexReceived(ArrayList<Region> regionsList) {
        for (Region region : regionsList) {
            SwitchCompat regionSwitch = (SwitchCompat) this.getLayoutInflater(null).inflate(R.layout.region_list_filter_item, null);

            regionSwitch.setText(region.name);
            regionSwitch.setTag(region.id);

            if (this.mFilter != null && this.mFilter.getRegions() != null
                    && this.mFilter.getRegions().contains(region.id)) {
                regionSwitch.setChecked(true);
            }

            this.mHolder.mRegionFilter.addView(regionSwitch);
        }
    }

    @Override
    public void onIndexError(int errorType) {

    }

    private class DateSwitchChangeListener implements CompoundButton.OnCheckedChangeListener {

        private ViewHolder mHolder;

        public DateSwitchChangeListener(ViewHolder holder) {
            this.mHolder = holder;
        }

        @Override
        public void onCheckedChanged(CompoundButton switcher, boolean isChecked) {
            if (isChecked) {
                if (switcher.getId() == this.mHolder.mTodayFilter.getId()) {
                    this.mHolder.mThisWeekFilter.setChecked(false);
                    this.mHolder.mThisMonthFilter.setChecked(false);
                } else if (switcher.getId() == this.mHolder.mThisWeekFilter.getId()) {
                    this.mHolder.mTodayFilter.setChecked(false);
                    this.mHolder.mThisMonthFilter.setChecked(false);
                } else if (switcher.getId() == this.mHolder.mThisMonthFilter.getId()) {
                    this.mHolder.mTodayFilter.setChecked(false);
                    this.mHolder.mThisWeekFilter.setChecked(false);
                }
            }
        }
    }

    private class FilterActionsClickListener implements View.OnClickListener {
        private ViewHolder mHolder;
        private EventFilterListener mListener;

        public FilterActionsClickListener(ViewHolder holder, EventFilterListener listener) {
            this.mHolder = holder;
            this.mListener = listener;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == this.mHolder.mApplyFilter.getId()) {
                this.applyFilters();
            }
            else if (view.getId() == this.mHolder.mClearFilter.getId()) {
                this.clearFilters();
            }
        }

        private void applyFilters() {
            ArrayList<Integer> selectedRegions = new ArrayList<Integer>();

            for (int i = 0; i < this.mHolder.mRegionFilter.getChildCount(); i++) {
                SwitchCompat regionSwitch = (SwitchCompat) this.mHolder.mRegionFilter.getChildAt(i);

                if (regionSwitch.isChecked()) {
                    selectedRegions.add((Integer)regionSwitch.getTag());
                }
            }

            EventFilterListener.DateFilter dateFilter = EventFilterListener.DateFilter.ALL;

            if (this.mHolder.mTodayFilter.isChecked()) {
                dateFilter = EventFilterListener.DateFilter.TODAY_FILTER;
            }
            else if (this.mHolder.mThisWeekFilter.isChecked()) {
                dateFilter = EventFilterListener.DateFilter.THIS_WEEK_FILTER;
            }
            else if (this.mHolder.mThisMonthFilter.isChecked()) {
                dateFilter = EventFilterListener.DateFilter.THIS_MONTH_FILTER;
            }

            this.mListener.onFilterSelected(this.mHolder.mEventFilter.getText().toString(), selectedRegions, dateFilter);
        }

        private void clearFilters() {
            this.mHolder.mEventFilter.setText("");
            this.mHolder.mEventFilter.clearFocus();

            for (int i = 0; i < this.mHolder.mRegionFilter.getChildCount(); i++) {
                SwitchCompat regionSwitch = (SwitchCompat) this.mHolder.mRegionFilter.getChildAt(i);

                regionSwitch.setChecked(false);
            }

            this.mHolder.mTodayFilter.setChecked(false);
            this.mHolder.mThisWeekFilter.setChecked(false);
            this.mHolder.mThisMonthFilter.setChecked(false);

            this.applyFilters();
        }
    }

    public interface EventFilterListener {

        public enum DateFilter {
            ALL,
            TODAY_FILTER,
            THIS_WEEK_FILTER,
            THIS_MONTH_FILTER
        }

        public void onFilterSelected(String nameFilter, ArrayList<Integer> regionFilter, DateFilter dateFilter);
    }

    private static class ViewHolder {
        public ScrollView mScrollView;
        public TextView mEventFilter;
        public LinearLayout mRegionFilter;
        public SwitchCompat mTodayFilter;
        public SwitchCompat mThisWeekFilter;
        public SwitchCompat mThisMonthFilter;
        public Button mApplyFilter;
        public Button mClearFilter;

        public ViewHolder(View view) {
            this.mScrollView = (ScrollView) view.findViewById(R.id.filter_scroll_view);
            this.mEventFilter = (TextView) view.findViewById(R.id.filter_name);
            this.mRegionFilter = (LinearLayout) view.findViewById(R.id.filter_region_list);
            this.mTodayFilter = (SwitchCompat) view.findViewById(R.id.filter_date_today);
            this.mThisWeekFilter = (SwitchCompat) view.findViewById(R.id.filter_date_this_week);
            this.mThisMonthFilter = (SwitchCompat) view.findViewById(R.id.filter_date_this_month);
            this.mApplyFilter = (Button) view.findViewById(R.id.filter_apply_button);
            this.mClearFilter = (Button) view.findViewById(R.id.filter_clear_button);
        }
    }
}
