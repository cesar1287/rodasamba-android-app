package com.ribeiro.cardoso.rodasamba;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.service.SetupDatabaseService;
import com.ribeiro.cardoso.rodasamba.util.SystemUiHider;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.util.ArrayList;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashScreenActivity extends Activity implements EventIndexInterface, Runnable {

    private static final int SPLASH_ANIMATION_LENGTH = 4000;
    private static final int SPLASH_DISPLAY_LENGTH = 5000;
    private static final int SPLASH_VENTURA_DISPLAY_LENGTH = 3000;
    private static final int NOT_CONNECTED_REQUEST_CODE = 1;

    private SetupServiceReceiver mReceiver;
    private EventBusiness mEventBusiness;
    private boolean mReachedTimeLimit = false;
    private boolean mEventsFetched = false;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private static String trackedFragmentName = "Carregando";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        //GOOGLE ANALYTICS
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-65658220-1");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(false);

        this.configureActivity();
    }

    private void configureActivity() {
        if(Utility.isFirstLaunch(this) || !Utility.isUserCreated(SplashScreenActivity.this)){

            if (Utility.isNetworkConnected(this)) {
                //this.startUserRegistrationActivity();
                Handler handler = new Handler();
                handler.postDelayed(this, 3000);
            }
            else {
                this.startNotConnectedActivity();
            }
        }else{
            if(Utility.isNetworkConnected(this)){
                // is connected, can fetch events on server

                if(Utility.isOccurringToday(Utility.getLastSync(this)) && Utility.getLastSync(this).getHourOfDay() > 9){
                    this.startMapActivity(false);
                }else{
                    this.startMapActivity(true);
                }
            }else{
                // not connected, use db fetch only

                if (Utility.getLastSync(this).plusDays(3).isBeforeNow()) {
                    // saved data is too old
                    this.startNotConnectedActivity();
                }
                else {
                    // can use saved data
                    this.startMapActivity(false);
                }
            }
        }
    }

    @Override
    public void run() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void startMapActivity(boolean onlineFetch) {
        if (onlineFetch) {
            String[] columns = new String[] {
                    SambaContract.EventEntry._ID,
                    SambaContract.EventEntry.COLUMN_THUMBNAIL_URL,
                    SambaContract.EventEntry.COLUMN_NAME,
                    SambaContract.EventEntry.COLUMN_EVENT_DATE,
                    SambaContract.EventEntry.COLUMN_TIME,
                    SambaContract.EventEntry.COLUMN_REGION_ID,
                    SambaContract.EventEntry.COLUMN_ADDRESS,
                    SambaContract.EventEntry.COLUMN_LATITUDE,
                    SambaContract.EventEntry.COLUMN_LONGITUDE,
                    SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID,
                    SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME
            };

            this.mEventBusiness = new EventBusiness(this, this, columns, false);

            this.mEventBusiness.getAsyncEventsRegionList();
        }
        else {
            SplashScreenActivity.this.mEventsFetched = true;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashScreenActivity.this.mReachedTimeLimit = true;

                if (SplashScreenActivity.this.mEventsFetched) {
                    SplashScreenActivity.this.startNewActivity();
                }
            }
        }, this.SPLASH_DISPLAY_LENGTH);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                long fadeDuration = 1000;

                AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(fadeDuration);

                RelativeLayout venturaLogo = (RelativeLayout)SplashScreenActivity.this.findViewById(R.id.ventura_logo);

                venturaLogo.startAnimation(animation);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout venturaLogo = (RelativeLayout)SplashScreenActivity.this.findViewById(R.id.ventura_logo);
                        ((RelativeLayout)venturaLogo.getParent()).removeView(venturaLogo);
                    }
                }, fadeDuration);
            }
        }, this.SPLASH_VENTURA_DISPLAY_LENGTH);
    }

    private void startUserRegistrationActivity() {
        /*mReceiver = new SetupServiceReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SetupDatabaseService.SERVICE_STATUS);
        registerReceiver(mReceiver, intentFilter);

        Intent setupDBintent = new Intent(this, SetupDatabaseService.class);
        startService(setupDBintent);*/
    }

    private void startNotConnectedActivity() {
        Intent intent = new Intent(this, NotConnectedActivity.class);

        this.startActivityForResult(intent, SplashScreenActivity.NOT_CONNECTED_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SplashScreenActivity.NOT_CONNECTED_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.configureActivity();
            }
            else {
                this.finish();
            }
        }
    }

    public void onStart() {
        super.onStart();
        this.configureAnimations();

        SplashScreenActivity.tracker.setScreenName(SplashScreenActivity.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void configureAnimations() {
        final RotateAnimation element1Animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        final RotateAnimation element2Animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        final RotateAnimation element3Animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        final RotateAnimation element4Animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);

        element1Animation.setDuration(SplashScreenActivity.SPLASH_ANIMATION_LENGTH);
        element2Animation.setDuration(SplashScreenActivity.SPLASH_ANIMATION_LENGTH);
        element3Animation.setDuration(SplashScreenActivity.SPLASH_ANIMATION_LENGTH);
        element4Animation.setDuration(SplashScreenActivity.SPLASH_ANIMATION_LENGTH);

        element1Animation.setRepeatCount(Animation.INFINITE);
        element2Animation.setRepeatCount(Animation.INFINITE);
        element3Animation.setRepeatCount(Animation.INFINITE);
        element4Animation.setRepeatCount(Animation.INFINITE);

        element1Animation.setInterpolator(new LinearInterpolator());
        element2Animation.setInterpolator(new LinearInterpolator());
        element3Animation.setInterpolator(new LinearInterpolator());
        element4Animation.setInterpolator(new LinearInterpolator());

        this.findViewById(R.id.nrds_element_1).setAnimation(element1Animation);
        this.findViewById(R.id.nrds_element_2).setAnimation(element2Animation);
        this.findViewById(R.id.nrds_element_3).setAnimation(element3Animation);
        this.findViewById(R.id.nrds_element_4).setAnimation(element4Animation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mReceiver){
            unregisterReceiver(mReceiver);
        }

        if (this.mEventBusiness != null) {
            this.mEventBusiness.unregisterReceivers();
            this.mEventBusiness = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.mEventBusiness != null) {
            this.mEventBusiness.unregisterReceivers();
            this.mEventBusiness = null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    protected void startNewActivity() {
        Intent intent;

        if (!Utility.isUserCreated(SplashScreenActivity.this)) {
            intent = new Intent(SplashScreenActivity.this, UserRegistrationActivity.class);
        }
        else {
            /*if (Utility.isNetworkConnected(SplashScreenActivity.this)) {
                intent = new Intent(SplashScreenActivity.this, EventsMapActivity.class);
            }
            else {
                intent = new Intent(SplashScreenActivity.this, EventsListActivity.class);
            }*/
            intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        }

        SplashScreenActivity.this.startActivity(intent);
        SplashScreenActivity.this.finish();
    }

    @Override
    public void onIndexReceived(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
        this.mEventsFetched = true;

        if (this.mReachedTimeLimit) {
            this.startNewActivity();
        }
    }

    @Override
    public void onIndexError(int errorType) {
        this.startNotConnectedActivity();
    }

    private static class SetupServiceReceiver extends BroadcastReceiver{
        private final SplashScreenActivity mActivity;

        public SetupServiceReceiver(SplashScreenActivity activity) {
            super();
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mActivity.startNewActivity();
        }
    }

}