package com.ribeiro.cardoso.rodasamba;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.api.UserSambaApi;
import com.ribeiro.cardoso.rodasamba.business.EventBusiness;
import com.ribeiro.cardoso.rodasamba.business.EventIndexInterface;
import com.ribeiro.cardoso.rodasamba.data.Entities.Event;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.Entities.User;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.util.SystemUiHider;
import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.util.dialog.AgeGroupDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.RegionDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.SexDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.UserRegistrationDialogFragment;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class UserRegistrationActivity extends FragmentActivity implements UserRegistrationDialogFragment.Listener, Callback<SambaApi.SambaApiResponse<User>>, EventIndexInterface {

    private final static String LOG_TAG = UserRegistrationActivity.class.getSimpleName();

    private TextView mRegionTextView;
    private TextView mAgeGroupTextView;
    private TextView mSexTextView;
    private Button mSubmitButton;

    private int mRegionIdSelected = 0;
    private int mAgeGroupIdSelected = 0;
    private String mSexKeySelected = null;

    public final static String REGION_KEY = "region";
    public final static String AGE_GROUP_KEY = "age_group";
    public final static String SEX_KEY = "sex";

    private int ATTEMPTS_TO_CONNECT = 0;
    private int MAX_ATTEMPTS_TO_CONNECT = 3;

    private UserRegistrationActivity mInstance;
    private ProgressDialog mProgressDialog;

    private static String trackedFragmentName = "Registro de Usuario";

    private EventBusiness mEventBusiness;

    Bundle infosFacebook;
    Bundle infosGoogle;
    String nome;
    String email;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_registration);

        mInstance = this;


        mRegionTextView = (TextView)findViewById(R.id.reg_region_text);

        mRegionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = RegionDialogFragment.newInstance(mInstance);
                dialogFragment.show(getSupportFragmentManager(), "dialog_region");
            }
        });

        mAgeGroupTextView = (TextView)findViewById(R.id.reg_age_group_text);

        mAgeGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = AgeGroupDialogFragment.newInstance(mInstance);
                dialogFragment.show(getSupportFragmentManager(), "dialog_age_group");
            }
        });

        mSexTextView = (TextView)findViewById(R.id.reg_sex_text);

        mSexTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = SexDialogFragment.newInstance(mInstance);
                dialogFragment.show(getSupportFragmentManager(), "dialog_sex");
            }
        });

        mSubmitButton = (Button) findViewById(R.id.reg_submit_button);
        //mSubmitButton.setEnabled(false);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAgeGroupIdSelected==0 || mSexKeySelected==null || mRegionIdSelected==0){
                    Toast.makeText(UserRegistrationActivity.this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show();
                }else {
                    sendUserToService();
                    finish();
                    startActivity(new Intent(UserRegistrationActivity.this, MainActivity.class));
                    //startActivity(new Intent(UserRegistrationActivity.this, LoginActivity.class));
                }
            }
        });

        infosFacebook = getIntent().getBundleExtra("infosFacebook");
        if(infosFacebook!=null) {
            nome = infosFacebook.get("first_name").toString()+" "+infosFacebook.get("last_name").toString();
            //email = infosFacebook.get("email").toString();
            id = infosFacebook.get("idFacebook").toString();
        }

        infosGoogle= getIntent().getBundleExtra("infosGoogle");
        if(infosGoogle!=null) {
            nome = infosGoogle.get("name").toString();
            //email = infosFacebook.get("email").toString();
            id = infosGoogle.get("id").toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SplashScreenActivity.tracker.setScreenName(UserRegistrationActivity.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void sendUserToService() {

        //Toast.makeText(this, getString(R.string.data_being_send), Toast.LENGTH_SHORT).show();

        User user = new User();
        user.nome = nome;
        if(id==null){
            id = "1";
        }else {
            user.id = id;
        }
        user.age_group_id = mAgeGroupIdSelected;
        user.region_id = mRegionIdSelected;
        user.sex = mSexKeySelected;
        user.device_os = "Android";
        user.device_name = Utility.getDeviceName();

        RegisterUser();

        /*UserSambaApi userSambaApi = new UserSambaApi();
        userSambaApi.post(user, mInstance);

        ATTEMPTS_TO_CONNECT++;*/
    }

    private void RegisterUser(){

        Utility.setUserId(this, id);
        Utility.setAgeGroupUser(this, mAgeGroupIdSelected);
        Utility.setRegionUser(this, mRegionIdSelected);
        Utility.setSexUser(this, mSexKeySelected);

        if (Utility.isUserCreated(this)){

            this.findViewById(R.id.user_registration_form_layout).setVisibility(View.GONE);
            this.findViewById(R.id.loading_screen_progress_layout).setVisibility(View.VISIBLE);

            Toast.makeText(this, getString(R.string.user_created_success), Toast.LENGTH_SHORT).show();

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
        }/*else {
            Toast.makeText(this, getString(R.string.common_google_play_services_network_error_text), Toast.LENGTH_SHORT);

            if (ATTEMPTS_TO_CONNECT < MAX_ATTEMPTS_TO_CONNECT) {
                sendUserToService();
            }
            else {
                Toast.makeText(this, getString(R.string.user_created_failed), Toast.LENGTH_SHORT).show();
            }

        }*/
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onItemSelected(String type, Object value, String name) {

        if (type.equals(SEX_KEY)){
            mSexKeySelected = value.toString();
            mSexTextView.setText(getString(R.string.sex_text) + " - " + name);
        }

        if (type.equals(REGION_KEY)){
            mRegionIdSelected =  Integer.parseInt(value.toString());
            mRegionTextView.setText(getString(R.string.region_text) + " - " + name);
        }

        if (type.equals(AGE_GROUP_KEY)){
            mAgeGroupIdSelected =  Integer.parseInt(value.toString());
            mAgeGroupTextView.setText(getString(R.string.age_group_text) + " - " + name);
        }

        if(mAgeGroupIdSelected > 0 && mRegionIdSelected > 0 && !mSexKeySelected.isEmpty()){
            mSubmitButton.setEnabled(true);
        }

    }


    @Override
    public void success(SambaApi.SambaApiResponse<User> userSambaApiResponse, Response response) {

        if(response.getStatus() == 201){
            for (Header header: response.getHeaders()){
                String name = header.getName();
                if (null != name && name.equals("Location")){
                    Utility.setUserId(this, header.getValue());
                    Utility.setAgeGroupUser(this, mAgeGroupIdSelected);
                    Utility.setRegionUser(this, mRegionIdSelected);
                    Utility.setSexUser(this, mSexKeySelected);
                    break;
                }
            }
        }

        if (Utility.isUserCreated(this)){

            this.findViewById(R.id.user_registration_form_layout).setVisibility(View.GONE);
            this.findViewById(R.id.loading_screen_progress_layout).setVisibility(View.VISIBLE);

            Toast.makeText(this, getString(R.string.user_created_success), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, getString(R.string.common_google_play_services_network_error_text), Toast.LENGTH_SHORT);

            if (ATTEMPTS_TO_CONNECT < MAX_ATTEMPTS_TO_CONNECT) {
                sendUserToService();
            }
            else {
                Toast.makeText(this, getString(R.string.user_created_failed), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {

        if (ATTEMPTS_TO_CONNECT < MAX_ATTEMPTS_TO_CONNECT){
            sendUserToService();
        }else{
            Toast.makeText(this, getString(R.string.user_created_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onIndexReceived(ArrayList<Utility.Pair<Event, Region>> eventsRegionList) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        this.finish();
    }

    @Override
    public void onIndexError(int errorType) {

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.mEventBusiness != null) {
            this.mEventBusiness.unregisterReceivers();
        }
    }
}
