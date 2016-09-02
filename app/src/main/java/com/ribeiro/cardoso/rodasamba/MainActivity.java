package com.ribeiro.cardoso.rodasamba;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import org.joda.time.DateTime;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements FilterFragment.EventFilterListener, GoogleApiClient.OnConnectionFailedListener {

    String[] mDrawerListItems;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewHolder mHolder;
    private Fragment mFragment;
    private FilterFragment mFilterFragment = null;
    private EventFilter mFilter;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.mDrawerListItems = this.getResources().getStringArray(R.array.nav_drawer_items);

        this.mHolder = new ViewHolder(this.findViewById(R.id.main_drawer_layout));

        this.mHolder.mSliderMenuListView.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, R.id.drawer_item_text, this.mDrawerListItems));
        this.mHolder.mSliderMenuListView.setOnItemClickListener(new DrawerItemClickListener(this));

        this.setupActionBar();

        if (Utility.isNetworkConnected(this)) {
            this.showFragment(EventsMapFragment.newInstance(true, this.mFilter));
        }
        else {
            this.showFragment(EventListFragment.newInstance(this.mFilter));
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            this.setSupportActionBar(toolbar);
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            this.getSupportActionBar().setHomeButtonEnabled(true);
            this.getSupportActionBar().setDisplayShowTitleEnabled(false);

            this.mDrawerToggle = new ActionBarDrawerToggle(
                    this,  this.mHolder.mDrawerLayout, toolbar,
                    R.string.drawer_open, R.string.drawer_close
            );

            this.mHolder.mDrawerLayout.setDrawerListener(mDrawerToggle);
            this.mDrawerToggle.syncState();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (this.mFragment.getClass() == EventListFragment.class || this.mFragment.getClass() == EventsMapFragment.class) {
            getMenuInflater().inflate(R.menu.menu_main, menu);

            if (this.mFilter != null) {
                menu.findItem(R.id.action_filter).setIcon(R.drawable.ic_filter_active);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            this.showFragment(FilterFragment.newInstance(this.mFilter));
            return true;
        }

        if (this.mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeDrawerLayout() {
        this.mHolder.mDrawerLayout.closeDrawer(this.mHolder.mSliderMenuLayout);
    }

    private void openDrawerLayout() {
        this.mHolder.mDrawerLayout.openDrawer(this.mHolder.mSliderMenuLayout);
    }

    public void showFragment(Fragment fragment) {
        if (fragment != null &&
                (this.mFragment == null || fragment.getClass() != this.mFragment.getClass())) {

            if (fragment.getClass() == FilterFragment.class) {
                if (this.mFilterFragment == null) {
                    this.getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fragment_up, R.anim.fragment_out)
                            .add(R.id.container, fragment)
                            .commit();

                    this.mFilterFragment = (FilterFragment) fragment;
                }
            }
            else {
                this.closeFilterFragment();

                this.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out)
                        .replace(R.id.container, fragment)
                        .commit();

                this.mFragment = fragment;
            }

            this.invalidateOptionsMenu();
        }

        this.closeDrawerLayout();
    }

    @Override
    public void onBackPressed() {
        if (this.mHolder.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mHolder.mDrawerLayout.closeDrawers();
        }
        else {
            if (this.mFilterFragment != null) {
                this.closeFilterFragment();
                this.invalidateOptionsMenu();
            }
            else {
                if (this.mFragment.getClass() == EventsMapFragment.class) {
                    super.onBackPressed();
                }
                else {
                    EventsMapFragment fragment = EventsMapFragment.newInstance(false, this.getFilter());

                    this.showFragment(fragment);
                }
            }
        }
    }

    private void closeFilterFragment() {
        if (this.mFilterFragment != null) {
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_out, R.anim.fragment_out)
                    .remove(this.mFilterFragment)
                    .commit();

            this.mFilterFragment = null;
        }
    }

    @Override
    public void onFilterSelected(String nameFilter, ArrayList<Integer> regionFilter, DateFilter dateFilter) {
        if ((nameFilter == null || nameFilter.trim().length() == 0)
                && (regionFilter == null || regionFilter.size() == 0)
                && (dateFilter == DateFilter.ALL)) {
            // Filter is clean
            this.mFilter = null;
        }
        else {
            // Filter is set
            this.mFilter = new EventFilter(nameFilter, regionFilter, dateFilter);
        }

        if (this.mFragment.getClass() == EventListFragment.class) {
            EventListFragment fragment = (EventListFragment) this.mFragment;

            fragment.setFilter(this.mFilter);
        }
        else if (this.mFragment.getClass() == EventsMapFragment.class) {
            EventsMapFragment fragment = (EventsMapFragment) this.mFragment;
            fragment.setFilter(this.mFilter);
        }

        this.closeFilterFragment();

        this.invalidateOptionsMenu();
    }

    public EventFilter getFilter() {
        return this.mFilter;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed:", ""+connectionResult);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        private MainActivity mActivity;

        public DrawerItemClickListener(MainActivity activity) {
            this.mActivity = activity;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            String[] drawerItems = MainActivity.this.getResources().getStringArray(R.array.nav_drawer_items);

            Fragment fragment = null;

            if (drawerItems[position].equals(MainActivity.this.getString(R.string.menu_item_about))
                    && !(this.mActivity.mFragment instanceof AboutFragment)) {
                // Fragment about
                fragment = AboutFragment.newInstance();
            }else if (drawerItems[position].equals(MainActivity.this.getString(R.string.menu_item_list))
                    && !(this.mActivity.mFragment instanceof EventListFragment)) {
                // Fragment list
                fragment = EventListFragment.newInstance(this.mActivity.getFilter());
            }else if (drawerItems[position].equals(MainActivity.this.getString(R.string.menu_item_map))
                    && !(this.mActivity.mFragment instanceof EventsMapFragment)) {
                // Fragment map
                fragment = EventsMapFragment.newInstance(false, this.mActivity.getFilter());
            }else if (drawerItems[position].equals(MainActivity.this.getString(R.string.menu_item_settings))
                    && !(this.mActivity.mFragment instanceof UserSettingsFragment)) {
                // Fragment setings
                fragment = UserSettingsFragment.newInstance();
            }else if (drawerItems[position].equals(MainActivity.this.getString(R.string.menu_item_logout))){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(R.string.message_logout)
                        .setPositiveButton(R.string.yes_logout, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setMessage("Saindo...");
                                progressDialog.show();
                                Thread mThread = new Thread(){
                                    @Override
                                    public void run() {
                                        //logout Google
                                        signOut();
                                        //logout Facebook
                                        LoginManager.getInstance().logOut();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                        progressDialog.dismiss();
                                        /*SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.clear();
                                        editor.apply();*/
                                        finish(); // dispose do java
                                    }
                                };
                                mThread.start();
                            }
                        })
                        .setNegativeButton(R.string.no_logout, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.show();
            }


            this.mActivity.showFragment(fragment);
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //nothing to do
                    }
                });
    }

    public class EventFilter {
        private String mName;
        private ArrayList<Integer> mRegions;
        private DateFilter mDate;

        public EventFilter(String name, ArrayList<Integer> regions, DateFilter date) {
            this.mName = name;
            this.mRegions = regions;
            this.mDate = date;
        }

        public String getName() {
            return mName;
        }

        public ArrayList<Integer> getRegions() {
            return mRegions;
        }

        public DateFilter getDate() {
            return mDate;
        }

        public boolean getThrough(Event event) {
            return filterByName(event) && filterByDate(event) && filterByRegion(event);
        }

        private boolean filterByName(Event event){
            return  (!this.isFilteredByName() || (event.name.toUpperCase().contains(this.getName().trim().toUpperCase())));
        }

        private boolean filterByDate(Event event) {
            boolean isInDateRange = false;

            if (this.getDate() == FilterFragment.EventFilterListener.DateFilter.ALL) {
                isInDateRange = true;
            }
            else {
                DateTime eventDate = Utility.getDateTimeFromDateString(event.date, event.time);

                if (this.getDate() == FilterFragment.EventFilterListener.DateFilter.TODAY_FILTER && Utility.isOccurringToday(eventDate)) {
                    isInDateRange = true;
                }
                else if (this.getDate() == FilterFragment.EventFilterListener.DateFilter.THIS_WEEK_FILTER && Utility.isOccurringThisWeek(eventDate)) {
                    isInDateRange = true;
                }
                else if (this.getDate() == FilterFragment.EventFilterListener.DateFilter.THIS_MONTH_FILTER && Utility.isOccurringThisMonth(eventDate)) {
                    isInDateRange = true;
                }
            }

            return isInDateRange;
        }

        private boolean filterByRegion(Event event){
            return (!this.isFilteredByRegion() || (this.getRegions() == null || this.getRegions().contains(event.region_id)));
        }

        private boolean isFilteredByName(){
            return  this.getName() != null && this.getName().trim().length() > 0;
        }

        private boolean isFilteredByRegion(){
            return this.getRegions() != null && this.getRegions().size() > 0;
        }
    }

    private class ViewHolder {

        public DrawerLayout mDrawerLayout;
        public LinearLayout mSliderMenuLayout;
        public ListView mSliderMenuListView;

        public ViewHolder(View view) {
            this.mDrawerLayout = (DrawerLayout) view.findViewById(R.id.main_drawer_layout);
            this.mSliderMenuLayout = (LinearLayout) view.findViewById(R.id.slidermenu_layout);
            this.mSliderMenuListView = (ListView) view.findViewById(R.id.slidermenu_list);
        }
    }
}
