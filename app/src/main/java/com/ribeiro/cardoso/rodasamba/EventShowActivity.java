package com.ribeiro.cardoso.rodasamba;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventShowInterface;
import com.ribeiro.cardoso.rodasamba.business.PictureBusiness;
import com.ribeiro.cardoso.rodasamba.business.PictureIndexInterface;
import com.ribeiro.cardoso.rodasamba.business.SpecialEventBusiness;
import com.ribeiro.cardoso.rodasamba.business.SpecialEventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Picture;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.Entities.SpecialEvent;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.util.ArrayList;


public class EventShowActivity extends ActionBarActivity {

    public final static String EVENT_ID_MESSAGE = "com.ribeiro.cardoso.rodasamba.EVENT_ID";
    private final static int NOT_CONNECTED_REQUEST_CODE = 1;

    private PlaceholderFragment mFragment;
    private int mRequestedEventId;

    private static String trackedFragmentName = "Detalhes de Evento";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_show);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            this.mRequestedEventId = this.getIntent().getIntExtra(EventShowActivity.EVENT_ID_MESSAGE, 0);
        }
        else {
            this.mRequestedEventId = savedInstanceState.getInt(EVENT_ID_MESSAGE);
        }

        this.mFragment = PlaceholderFragment.newInstance(this.mRequestedEventId);

        this.getSupportFragmentManager().beginTransaction()
                .add(R.id.container, this.mFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EVENT_ID_MESSAGE, this.mRequestedEventId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu_show, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            /*case R.id.menu_item_share:
                this.mFragment.shareEventUrl();
                return true;*/
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final int LOAD_DISPLAY_LENGTH = 4000;
        private static final int MAX_CALENDAR_EVENTS = 3;
        private static final String STORED_EVENT_REGION = "com.ribeiro.cardoso.rodasamba.STORED_EVENT_REGION";

        private int mRequestedEventId;
        private EventBusiness mEventBusiness;
        private SpecialEventBusiness mSpecialEventBusiness;
        private PictureBusiness mPictureBusiness;

        private Utility.Pair<Event,Region> mEventRegion;
        private ArrayList<SpecialEvent> mSpecialEvents;
        private ArrayList<Picture> mPictures;

        private Handler mPictureFlipHandler;
        private Runnable mPictureFlipRunnable;
        private int mPictureFlipDirection = 1;
        private boolean mPictureFlipFirst = true;

        public static final String[] COLUMNS = new String[]{
                SambaContract.EventEntry._ID,
                SambaContract.EventEntry.COLUMN_THUMBNAIL_URL,
                SambaContract.EventEntry.COLUMN_NAME,
                SambaContract.EventEntry.COLUMN_EVENT_DATE,
                SambaContract.EventEntry.COLUMN_TIME,
                SambaContract.EventEntry.COLUMN_REGION_ID,
                SambaContract.EventEntry.COLUMN_ADDRESS,
                SambaContract.EventEntry.COLUMN_DESCRIPTION,
                SambaContract.EventEntry.COLUMN_TICKET_PRICE,
                SambaContract.EventEntry.COLUMN_DRINK_PRICE,
                SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME
        };

        private ViewHolder mHolder;

        public PlaceholderFragment () {
            if (this.getActivity() == null) {
                //Activity nula, ignorar...
            }
        }

        public static PlaceholderFragment newInstance(int requestedEventId) {
            PlaceholderFragment f =  new PlaceholderFragment();

            f.mRequestedEventId = requestedEventId;
            f.mSpecialEvents = null;
            f.mEventRegion = null;

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_event_show, container, false);
            this.mHolder = new ViewHolder(rootView);

            this.configureActivity();

            return rootView;
        }

        public void configureActivity() {
            this.cancelImageFlipHandlerIfSet();

            if (Utility.isNetworkConnected(this.getActivity())) {
                this.mEventBusiness = new EventBusiness(new EventShowReceiver(), this.getActivity(), COLUMNS, false);
                this.mEventBusiness.getAsyncEvent(this.mRequestedEventId);

                this.mSpecialEventBusiness = new SpecialEventBusiness(new SpecialEventIndexReceiver(), this.getActivity());
                this.mSpecialEventBusiness.getAsyncSpecialEventsList(this.mRequestedEventId, MAX_CALENDAR_EVENTS);

                this.mPictureBusiness = new PictureBusiness(new PictureIndexReceiver(), this.getActivity());
                this.mPictureBusiness.getAsyncPicturesList(this.mRequestedEventId);

                this.mHolder.mListLoadingView.setVisibility(View.VISIBLE);
                this.mHolder.mListLoadedView.setVisibility(View.GONE);
            }
            else {
                Intent intent = new Intent(this.getActivity(), NotConnectedActivity.class);

                this.startActivityForResult(intent, EventShowActivity.NOT_CONNECTED_REQUEST_CODE);
            }
        }

        private void cancelImageFlipHandlerIfSet() {
            if (this.mPictureFlipHandler != null && this.mPictureFlipRunnable != null) {
                this.mPictureFlipHandler.removeCallbacks(this.mPictureFlipRunnable);
            }
        }

        public void shareEventUrl() {
            if (this.mEventRegion != null) {
                Intent i = new Intent(Intent.ACTION_SEND);

                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, this.mEventRegion.getLeft().name);
                i.putExtra(Intent.EXTRA_TEXT, SambaApi.BASE_URL + "event/" + this.mRequestedEventId);

                this.startActivity(Intent.createChooser(i, this.getString(R.string.menu_item_share)));
            }
        }

        public void navigateEventUrl() {
            if (this.mEventRegion != null) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + this.mEventRegion.getLeft().address));
                this.startActivity(i);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == EventShowActivity.NOT_CONNECTED_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    this.configureActivity();
                }
                else {
                    this.getActivity().finish();
                }
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            RotateAnimation loadingAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
            loadingAnimation.setDuration(PlaceholderFragment.LOAD_DISPLAY_LENGTH);
            loadingAnimation.setRepeatCount(Animation.INFINITE);
            loadingAnimation.setInterpolator(new LinearInterpolator());

            this.getActivity().findViewById(R.id.events_list_loading_image).setAnimation(loadingAnimation);
            this.getActivity().findViewById(R.id.events_list_loading_view).setVisibility(View.VISIBLE);
            this.getActivity().findViewById(R.id.events_list_loaded_view).setVisibility(View.GONE);
        }

        @Override
        public void onStop() {
            super.onStop();

            if (this.mEventBusiness != null) {
                this.mEventBusiness.unregisterReceivers();
            }

            this.cancelImageFlipHandlerIfSet();
        }

        private void showData() {
            Event event = this.mEventRegion.getLeft();
            Region region = this.mEventRegion.getRight();

            SplashScreenActivity.tracker.setScreenName(EventShowActivity.trackedFragmentName + " - " + event.name);
            SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());

            this.mHolder.mShareButtonLayout.setClickable(true);
            this.mHolder.mShareButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareEventUrl();
                }
            });

            this.mHolder.mNavigateButtonLayout.setClickable(true);
            this.mHolder.mNavigateButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateEventUrl();
                }
            });

            this.mPictures.add(0, this.getEventThumbnailAsPicture());
            this.mHolder.mEventImagesViewPager.setAdapter(new PicturesSlidePageAdapter(this.getActivity().getSupportFragmentManager(), this.mPictures));
            if (this.mPictures.size() > 1) {
                this.configurePictureFlip();
                this.mHolder.mEventImagesViewPager.setOnPageChangeListener(new EventPicturesPageChangeListener());
            }

            this.mHolder.mListLoadingView.setVisibility(View.GONE);
            this.mHolder.mListLoadedView.setVisibility(View.VISIBLE);

            //this.setTitleFontFace(this.mHolder);

            this.mHolder.mEventNameText.setText(event.name);
            this.mHolder.mEventDay.setText(Utility.getDayFromDateString(event.date));
            this.mHolder.mEventMonth.setText(Utility.getFullMonthFromDateString(event.date));
            this.mHolder.mEventHour.setText(Utility.getHourFromTimeString(event.time) + ":");
            this.mHolder.mEventMinute.setText(Utility.getMinuteFromTimeString(event.time));
            this.mHolder.mEventAddress.setText(event.address);

            if (event.description == null || event.description.isEmpty()) {
                this.mHolder.mEventDescriptionSection.setVisibility(View.GONE);
            }
            else {
                this.mHolder.mEventDescription.setText(event.description);
                this.mHolder.mEventDescriptionSection.setVisibility(View.VISIBLE);
            }

            if (event.ticket_price == null || event.ticket_price.length() == 0) {
                this.mHolder.mEventTicketPriceSection.setVisibility(View.GONE);
            }
            else {
                this.mHolder.mEventTicketPrice.setText(event.ticket_price);
                this.mHolder.mEventTicketPriceSection.setVisibility(View.VISIBLE);
            }

            if (event.drink_price == null || event.drink_price.length() == 0) {
                this.mHolder.mEventDrinkPriceSection.setVisibility(View.GONE);
            }
            else {
                this.mHolder.mEventDrinkPrice.setText(event.drink_price);
                this.mHolder.mEventDrinkPriceSection.setVisibility(View.VISIBLE);
            }

            if (this.mSpecialEvents != null && this.mSpecialEvents.size() > 0) {

                for (SpecialEvent specialEvent : this.mSpecialEvents) {
                    View calendarView = this.getLayoutInflater(null).inflate(R.layout.event_calendar_item, null);

                    TextView itemDate = (TextView)calendarView.findViewById(R.id.event_calendar_item_date);
                    TextView itemTime = (TextView)calendarView.findViewById(R.id.event_calendar_item_time);

                    itemDate.setText(Utility.getDayFromDateString(specialEvent.date) + Utility.getMonthFromDateString(specialEvent.date));
                    itemTime.setText(Utility.getHoursAndMinutesFromTimeString(this.mEventRegion.getLeft().time));

                    this.mHolder.mEventCalendarWraper.addView(calendarView);
                }

                this.mHolder.mEventCalendarSection.setVisibility(View.VISIBLE);
            }
            else {
                this.mHolder.mEventCalendarSection.setVisibility(View.GONE);
            }
        }

        private void configurePictureFlip() {
            this.mPictureFlipHandler = new Handler();
            this.mPictureFlipRunnable = new Runnable() {
                @Override
                public void run() {
                    ViewPager pager = PlaceholderFragment.this.mHolder.mEventImagesViewPager;

                    if (!PlaceholderFragment.this.mPictureFlipFirst) {
                        if (pager.getCurrentItem() == 0 || pager.getCurrentItem() == PlaceholderFragment.this.mPictures.size() - 1) {
                            PlaceholderFragment.this.mPictureFlipDirection *= -1;
                        }
                    }
                    else {
                        PlaceholderFragment.this.mPictureFlipFirst = false;
                    }

                    pager.setCurrentItem(pager.getCurrentItem() + PlaceholderFragment.this.mPictureFlipDirection, true);

                    PlaceholderFragment.this.configurePictureFlip();
                }
            };

            this.mPictureFlipHandler.postDelayed(this.mPictureFlipRunnable, 15000);
        }

        private Picture getEventThumbnailAsPicture() {
            Picture thumbPicture = new Picture();

            thumbPicture.event_id = this.mRequestedEventId;
            thumbPicture.url = this.mEventRegion.getLeft().thumbnail_url;

            return thumbPicture;
        }

        private void setTitleFontFace(ViewHolder holder) {
            Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Contexto.ttf");

            holder.mEventNameText.setTypeface(font);
            holder.mDescriptionTitleText.setTypeface(font);
            holder.mTicketTitleText.setTypeface(font);
            holder.mDrinkTitleText.setTypeface(font);
            holder.mNextDatesTitleText.setTypeface(font);
        }

        private class EventShowReceiver implements EventShowInterface {

            @Override
            public void onShowReceived(Utility.Pair<Event, Region> eventRegion) {
                PlaceholderFragment.this.mEventRegion = eventRegion;

                if (PlaceholderFragment.this.mSpecialEvents != null && PlaceholderFragment.this.mPictures != null) {
                    PlaceholderFragment.this.showData();
                }
            }

            @Override
            public void onShowError(int errorType) {

            }
        }

        private class SpecialEventIndexReceiver implements SpecialEventIndexInterface {

            @Override
            public void onIndexReceived(ArrayList<SpecialEvent> specialEvents) {
                PlaceholderFragment.this.mSpecialEvents = specialEvents;

                if (PlaceholderFragment.this.mEventRegion != null && PlaceholderFragment.this.mPictures != null) {
                    PlaceholderFragment.this.showData();
                }
            }

            @Override
            public void onIndexError(int errorType) {

            }
        }

        private class PictureIndexReceiver implements PictureIndexInterface {

            @Override
            public void onIndexReceived(ArrayList<Picture> pictures) {
                PlaceholderFragment.this.mPictures = pictures;

                if (PlaceholderFragment.this.mEventRegion != null && PlaceholderFragment.this.mSpecialEvents != null) {
                    PlaceholderFragment.this.showData();
                }
            }

            @Override
            public void onIndexError(int errorType) {

            }
        }

        private class PicturesSlidePageAdapter extends FragmentStatePagerAdapter {

            private ArrayList<Picture> mEventPictures;

            public PicturesSlidePageAdapter(FragmentManager fm, ArrayList<Picture> eventImages) {
                super(fm);

                this.mEventPictures = eventImages;
            }

            @Override
            public Fragment getItem(int i) {
                EventPictureFragment pictureFragment = EventPictureFragment.newInstance(this.mEventPictures.get(i).url);

                return pictureFragment;
            }

            @Override
            public int getCount() {
                return this.mEventPictures != null ? this.mEventPictures.size() : 0;
            }
        }

        private class EventPicturesPageChangeListener implements ViewPager.OnPageChangeListener {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                PlaceholderFragment.this.cancelImageFlipHandlerIfSet();
                PlaceholderFragment.this.configurePictureFlip();
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        }

        private static class ViewHolder {
            public final ViewPager mEventImagesViewPager;
            public final ImageView mPlaceholderImage;

            public final LinearLayout mShareButtonLayout;
            public final LinearLayout mNavigateButtonLayout;

            public final RelativeLayout mListLoadingView;
            public final LinearLayout mListLoadedView;

            public final TextView mEventNameText;
            public final TextView mEventDay;
            public final TextView mEventMonth;
            public final TextView mEventHour;
            public final TextView mEventMinute;
            public final TextView mEventAddress;

            public final LinearLayout mEventDescriptionSection;
            public final TextView mEventDescription;

            public final LinearLayout mEventTicketPriceSection;
            public final TextView mEventTicketPrice;
            public final LinearLayout mEventDrinkPriceSection;
            public final TextView mEventDrinkPrice;

            public final TextView mDescriptionTitleText;
            public final TextView mTicketTitleText;
            public final TextView mDrinkTitleText;
            public final TextView mNextDatesTitleText;

            public final LinearLayout mEventCalendarSection;
            public final LinearLayout mEventCalendarWraper;

            public ViewHolder(View view) {
                this.mPlaceholderImage = (ImageView) view.findViewById(R.id.event_show_placeholder_image);
                this.mEventImagesViewPager = (ViewPager) view.findViewById(R.id.event_show_images_viewpager);

                mShareButtonLayout = (LinearLayout) view.findViewById(R.id.event_show_share_button_layout);
                mNavigateButtonLayout = (LinearLayout) view.findViewById(R.id.event_show_navigate_button_layout);

                mListLoadingView = (RelativeLayout)view.findViewById(R.id.events_list_loading_view);
                mListLoadedView = (LinearLayout)view.findViewById(R.id.events_list_loaded_view);

                mEventNameText = (TextView) view.findViewById(R.id.event_name_text);
                mEventDay = (TextView) view.findViewById(R.id.event_show_day);
                mEventMonth = (TextView) view.findViewById(R.id.event_show_month);
                mEventHour = (TextView) view.findViewById(R.id.event_show_hour);
                mEventMinute = (TextView) view.findViewById(R.id.event_show_minute);
                mEventAddress = (TextView) view.findViewById(R.id.event_show_address);
                mEventDescriptionSection = (LinearLayout) view.findViewById(R.id.event_show_description_section);
                mEventDescription = (TextView) view.findViewById(R.id.event_show_description);
                mEventTicketPriceSection = (LinearLayout) view.findViewById(R.id.event_show_ticket_price_section);
                mEventTicketPrice = (TextView) view.findViewById(R.id.event_show_ticket_price);
                mEventDrinkPriceSection = (LinearLayout) view.findViewById(R.id.event_show_drink_price_section);
                mEventDrinkPrice = (TextView) view.findViewById(R.id.event_show_drink_price);

                mDescriptionTitleText = (TextView) view.findViewById(R.id.description_title_text);
                mTicketTitleText = (TextView) view.findViewById(R.id.ticket_title_text);
                mDrinkTitleText = (TextView) view.findViewById(R.id.drink_title_text);
                mNextDatesTitleText = (TextView) view.findViewById(R.id.next_dates_title_text);

                mEventCalendarSection = (LinearLayout) view.findViewById(R.id.event_show_calendar_section);
                mEventCalendarWraper = (LinearLayout) view.findViewById(R.id.event_show_calendar_wraper);
            }
        }
    }
}
