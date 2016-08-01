package com.ribeiro.cardoso.rodasamba;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.util.Utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vinicius.ribeiro on 24/11/2014.
 */
public class EventsMapFragment extends Fragment implements LocationListener, EventIndexInterface {
    private static final String LOG_TAG = EventsMapFragment.class.getSimpleName();

    private static final float MAP_ZOOM = 13;
    public static final int MAP_PADDING = 200;
    public static final int MOVE_CAMERA_DURATION_MS = 300;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;
    private Map<Marker, Utility.Pair<Event, Region>> mMarkerEventMap;
    private List<Utility.Pair<Event, Region>> mCompleteEventsRegionList;
    private boolean mAnimateCamera = false;

    private static String trackedFragmentName = "Mapa de Eventos";

    private EventBusiness mEventBusiness;

    private boolean mMoveCamera;

    private static final String[] COLUMNS = new String[]{
            SambaContract.EventEntry._ID,
            SambaContract.EventEntry.COLUMN_THUMBNAIL_URL,
            SambaContract.EventEntry.COLUMN_NAME,
            SambaContract.EventEntry.COLUMN_EVENT_DATE,
            SambaContract.EventEntry.COLUMN_TIME,
            SambaContract.EventEntry.COLUMN_REGION_ID,
            SambaContract.EventEntry.COLUMN_ADDRESS,
            SambaContract.EventEntry.COLUMN_LATITUDE,
            SambaContract.EventEntry.COLUMN_LONGITUDE,
            SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry.COLUMN_NAME,
            SambaContract.RegionEntry.TABLE_NAME + "_" + SambaContract.RegionEntry._ID
    };
    private View mLoadingView;
    private View mLayoutMapView;
    private MainActivity.EventFilter mFilter;

    public EventsMapFragment() {

    }

    public static EventsMapFragment newInstance(boolean animateCamera, MainActivity.EventFilter filter) {
        EventsMapFragment instance = new EventsMapFragment();

        instance.mAnimateCamera = animateCamera;
        instance.mFilter = filter;

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_map, container, false);

        mMarkerEventMap = new HashMap<Marker, Utility.Pair<Event, Region>>();

        mLoadingView = rootView.findViewById(R.id.events_map_loading_view);
        mLayoutMapView = rootView.findViewById(R.id.events_map_layout_view);

        mLoadingView.setVisibility(View.GONE);
        mLayoutMapView.setVisibility(View.VISIBLE);

        this.mEventBusiness = new EventBusiness(this, this.getActivity(), COLUMNS, true);

        this.mMoveCamera = true;

        setUpMapIfNeeded();

        Log.d(LOG_TAG, "onCreateView");

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        mEventBusiness.unregisterReceivers();
        mLocationManager = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        mEventBusiness.unregisterReceivers();
        mLocationManager = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        SplashScreenActivity.tracker.setScreenName(EventsMapFragment.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity()) != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity()), this.getActivity(), 0).show();
        } else {
            setUpMapIfNeeded();
            if (!Utility.isUserCreated(this.getActivity())) {
                Intent intent = new Intent(this.getActivity(), UserRegistrationActivity.class);
                startActivity(intent);
            }
        }
    }

    public void setFilter(MainActivity.EventFilter filter) {
        this.mFilter = filter;
        this.showEventsOnMap(false);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
        MapsInitializer.initialize(this.getActivity());
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(new EventMarkerClickListener());

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                EventsMapFragment.this.mEventBusiness.getAsyncEventsRegionList();
                EventsMapFragment.this.mMap.setOnCameraChangeListener(null);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        moveMapToLocation(location);

        mLocationManager.removeUpdates(this);
    }

    private void moveMapToLocation(Location location) {
        double  lat = location.getLatitude(),
                lg = location.getLongitude();

        LatLng latLng = new LatLng(lat, lg);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        if (this.mAnimateCamera) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM));
        }

        mLocationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onIndexReceived(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
        mLoadingView.setVisibility(View.GONE);
        mLayoutMapView.setVisibility(View.VISIBLE);

        this.mCompleteEventsRegionList = eventsRegionList;

        showEventsOnMap(false);
    }

    protected void showEventsOnMap(boolean showAllPins) {

        mMap.clear();

        if (this.mCompleteEventsRegionList != null) {

            List<Utility.Pair<Event, Region>> eventsRegionList = getFilterRegionList();

            for (Utility.Pair<Event, Region> eventRegionPair : eventsRegionList) {

                Event event = eventRegionPair.getLeft();

                LatLng position = new LatLng(event.latitude, event.longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.flat(false);

                boolean isToday = Utility.isOccurringToday(Utility.getDateTimeFromDateString(event.date, event.time));

                if (isToday) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin));
                }
                else {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_off));
                }
                markerOptions.position(position);

                Marker marker = mMap.addMarker(markerOptions);

                if (isToday) marker.showInfoWindow();

                mMarkerEventMap.put(marker, eventRegionPair);
            }

            if(this.mMoveCamera) {
                if (showAllPins) {
                    moveCameraToViewPins();
                }
                else {
                    Region region = new Region();
                    region.id = Utility.getRegionUser(this.getActivity());

                    moveCameraToRegionPins(region, eventsRegionList);
                }
            }

            this.mMoveCamera = true;
        }
    }

    private List<Utility.Pair<Event, Region>> getFilterRegionList() {

        if(this.mFilter == null){
            return mCompleteEventsRegionList;
        }

        List<Utility.Pair<Event, Region>> eventsRegionList = new ArrayList<Utility.Pair<Event, Region>>();

        for (Utility.Pair<Event, Region> eventRegion: this.mCompleteEventsRegionList) {
            Event event = eventRegion.getLeft();

            if (this.mFilter == null || (this.mFilter != null && this.mFilter.getThrough(event))){
                eventsRegionList.add(eventRegion);
            }
        }

        return  eventsRegionList;
    }

    private void moveCameraToViewPins(){

        List<Event> eventsList = new ArrayList<Event>();

        for (Utility.Pair<Event, Region> eventRegionPair: this.mMarkerEventMap.values()){
            eventsList.add(eventRegionPair.getLeft());
        }

        this.moveCameraToViewPins(eventsList);
    }

    private void moveCameraToViewPins(List<Event> events){
        LatLngBounds boundsBuild = null;

        for(Event event: events){
            LatLng latLng = new LatLng(event.latitude, event.longitude);

            if (boundsBuild == null) {
                boundsBuild = new LatLngBounds(latLng, latLng);
            }
            else {
                boundsBuild = boundsBuild.including(latLng);
            }
        }

        if (boundsBuild != null){
            if (this.mAnimateCamera) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuild, MAP_PADDING));
            }
            else {
                mMap.moveCamera((CameraUpdateFactory.newLatLngBounds(boundsBuild, MAP_PADDING)));
            }
        }
        else{
            Toast.makeText(this.getActivity(), "Nenhum evento encontrado.", Toast.LENGTH_SHORT);
        }

    }

    private void moveCameraToRegionPins(Region region, List<Utility.Pair<Event, Region>> eventsToSearch){
        List<Event> eventsToShow = new ArrayList<Event>();

        for (Utility.Pair<Event, Region> eventRegionPair: eventsToSearch){
            Event event = eventRegionPair.getLeft();
            if (event.region_id == region.id){
                eventsToShow.add(event);
            }
        }

        if (eventsToShow.size() > 0){
            moveCameraToViewPins(eventsToShow);
        }else{
            moveCameraToViewPins();
        }
    }

    @Override
    public void onIndexError(int errorType) {

    }

    private class EventMarkerClickListener implements GoogleMap.OnMarkerClickListener {

        public static final int PRECISION_POSITION = 5;

        @Override
        public boolean onMarkerClick(final Marker marker) {

            //Não sei porque razão, os valores de latitude e longitude não aparecem iguais, mesmo quando é um segundo clique no mesmo marcador
            LatLng mapLatLng = mMap.getCameraPosition().target;
            Double mapLagWithPrecision = new BigDecimal(mapLatLng.latitude).setScale(PRECISION_POSITION, BigDecimal.ROUND_CEILING).doubleValue();
            Double mapLngWithPrecision = new BigDecimal(mapLatLng.longitude).setScale(PRECISION_POSITION, BigDecimal.ROUND_CEILING).doubleValue();

            final LatLng markerPosition = marker.getPosition();
            Double markerLagWithPrecision = new BigDecimal(markerPosition.latitude).setScale(PRECISION_POSITION, BigDecimal.ROUND_CEILING).doubleValue();
            Double markerLngWithPrecision = new BigDecimal(markerPosition.longitude).setScale(PRECISION_POSITION, BigDecimal.ROUND_CEILING).doubleValue();

            if ((mapLagWithPrecision - markerLagWithPrecision == 0.0) && (mapLngWithPrecision - markerLngWithPrecision == 0.0)){
                startEventInfoActivity(markerPosition);
            }else{

                mMap.animateCamera(CameraUpdateFactory.newLatLng(markerPosition), MOVE_CAMERA_DURATION_MS, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        startEventInfoActivity(markerPosition);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

            }

            return true;
        }

        protected void startEventInfoActivity(LatLng markerPosition) {
            Intent intent = new Intent(EventsMapFragment.this.getActivity(), EventsMapInfoActivity.class);
            Bundle bundle = new Bundle();

            bundle.putDouble(EventsMapInfoActivity.EXTRA_LATITUDE, markerPosition.latitude);
            bundle.putDouble(EventsMapInfoActivity.EXTRA_LONGITUDE, markerPosition.longitude);

            intent.putExtras(bundle);
            startActivity(intent);


            EventsMapFragment.this.mMoveCamera = false;
        }
    }
}
