package com.ribeiro.cardoso.rodasamba;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.api.UserSambaApi;
import com.ribeiro.cardoso.rodasamba.data.Entities.User;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;
import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.util.dialog.AgeGroupDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.RegionDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.SexDialogFragment;
import com.ribeiro.cardoso.rodasamba.util.dialog.UserRegistrationDialogFragment;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettingsFragment extends Fragment implements UserRegistrationDialogFragment.Listener, Callback<SambaApi.SambaApiResponse<User>> {

    private final String LOG_TAG = UserSettingsFragment.class.getSimpleName();

    private final String SEX = "sex";
    private final String AGE_GROUP = "age_group";
    private final String REGION = "region";

    private static String trackedFragmentName = "Configuracoes";

    private View mSexLayout;
    private TextView mSexTextView;
    private View mAgeGroupLayout;
    private TextView mAgeGroupTextView;
    private View mRegionLayout;
    private TextView mRegionTextView;
    private Button mSalvarButton;
    private SQLiteDatabase mSqlClient;

    private int CONNECTIONS_TRIED = 0;
    private final int MAX_CONNECTIONS_TO_TRY = 3;

    private User mUser;


    private UserRegistrationDialogFragment.Listener mListener;
    private UserSambaApi mUserSambaApi;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment UserSettingsFragment.
     */
    public static UserSettingsFragment newInstance() {
        UserSettingsFragment fragment = new UserSettingsFragment();

        return fragment;
    }

    public UserSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        SplashScreenActivity.tracker.setScreenName(UserSettingsFragment.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mListener = this;

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_settings, container, false);

        loadFragmentsFields(rootView);

        setSettingsListeners();

        SambaDbHelper sambaDbHelper = SambaDbHelper.getInstance(getActivity());
        mSqlClient = sambaDbHelper.getReadableDatabase();

        //Inicializando o objeto da API
        mUserSambaApi = new UserSambaApi();

        initUser();

        String ageGroupText = fetchUserAgeGroupFromDbById(mUser.age_group_id);
        setAgeGroupText(ageGroupText);

        String sexText = fetchUserSexFromDbById(mUser.sex);
        setSexText(sexText);

        String regionText = fetchUserRegionFromDbById(mUser.region_id);
        setRegionText(regionText);

        return rootView;
    }

    private void initUser() {
        mUser = new User();
        mUser.id = Utility.getUserId(this.getActivity());
        mUser.age_group_id = Utility.getAgeGroupUser(this.getActivity());
        mUser.region_id = Utility.getRegionUser(this.getActivity());
        mUser.sex = Utility.getSexUser(this.getActivity());
        mUser.device_os = "Android";
        mUser.device_name = Utility.getDeviceName();
    }

    private void saveUserToPreferences(){
        Utility.setAgeGroupUser(this.getActivity(), mUser.age_group_id);
        Utility.setRegionUser(this.getActivity(), mUser.region_id);
        Utility.setSexUser(this.getActivity(), mUser.sex);
    }

    private void setAgeGroupText(String ageGroupText) {
        mAgeGroupTextView.setText(ageGroupText);
    }

    private String fetchUserAgeGroupFromDbById(int ageGroupId) {
        final Cursor ageGroupCursor = mSqlClient.query(
                                              SambaContract.AgeGroupEntry.TABLE_NAME //Table AgeGroup
                                            , new String[]{SambaContract.AgeGroupEntry.COLUMN_NAME} //Getting the Column NAME
                                            , SambaContract.AgeGroupEntry._ID + " = ?" //Where clause
                                            , new String[]{String.valueOf( ageGroupId )} //Where value
                                            , null, null, null);
        ageGroupCursor.moveToNext();
        int idxNameColumn = ageGroupCursor.getColumnIndex(SambaContract.AgeGroupEntry.COLUMN_NAME);
        return ageGroupCursor.getString(idxNameColumn);
    }

    private void setSexText(String sexText) {
        mSexTextView.setText(sexText);
    }

    private String fetchUserSexFromDbById(String sexKey) {
        final Cursor sexCursor = mSqlClient.query(
                SambaContract.SexEntry.TABLE_NAME //Table Sex
                , new String[]{SambaContract.SexEntry.COLUMN_NAME} //Getting the Column NAME
                , SambaContract.SexEntry.COLUMN_KEY + " = ?" //Where clause
                , new String[]{ sexKey } //Where value
                , null, null, null);
        sexCursor.moveToNext();
        int idxNameColumn = sexCursor.getColumnIndex(SambaContract.SexEntry.COLUMN_NAME);
        return sexCursor.getString(idxNameColumn);
    }

    private void setRegionText(String regionText) {
        mRegionTextView.setText(regionText);
    }

    private String fetchUserRegionFromDbById(int regionId) {
        final Cursor regionCursor = mSqlClient.query(
                SambaContract.RegionEntry.TABLE_NAME //Table Sex
                , new String[]{SambaContract.RegionEntry.COLUMN_NAME} //Getting the Column NAME
                , SambaContract.RegionEntry._ID + " = ?" //Where clause
                , new String[]{String.valueOf( regionId )} //Where value
                , null, null, null);
        regionCursor.moveToNext();
        int idxNameColumn = regionCursor.getColumnIndex(SambaContract.RegionEntry.COLUMN_NAME);
        return regionCursor.getString(idxNameColumn);
    }

    private void loadFragmentsFields(View rootView) {
        mSexLayout = rootView.findViewById(R.id.dialog_sex);
        mSexTextView = (TextView) rootView.findViewById(R.id.user_sex_text_value);
        mAgeGroupLayout = rootView.findViewById(R.id.dialog_age_group);
        mAgeGroupTextView = (TextView) rootView.findViewById(R.id.user_age_group_text_value);
        mRegionLayout = rootView.findViewById(R.id.dialog_region);
        mRegionTextView = (TextView) rootView.findViewById(R.id.user_region_text_value);
        mSalvarButton = (Button)rootView.findViewById(R.id.reg_submit_button);
    }

    private void setSettingsListeners() {
        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = SexDialogFragment.newInstance(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog_sex");
            }
        });

        mAgeGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = AgeGroupDialogFragment.newInstance(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog_age_group");
            }
        });

        mRegionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = RegionDialogFragment.newInstance(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog_region");
            }
        });

        mSalvarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserSettingsFragment.this.getActivity(), getString(R.string.user_save_on_progress), Toast.LENGTH_LONG).show();
                CONNECTIONS_TRIED = 1;
                mUserSambaApi.put(mUser, UserSettingsFragment.this);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onItemSelected(String type, Object value, String name) {
        if (type.equals(SEX)){
            setSexText(name);
            mUser.sex = value.toString();
        }

        if (type.equals(REGION)){
            setRegionText(name);
            mUser.region_id = Integer.parseInt(value.toString());
        }

        if (type.equals(AGE_GROUP)){
            setAgeGroupText(name);
            mUser.age_group_id = Integer.parseInt(value.toString());
        }
    }

    @Override
    public void success(SambaApi.SambaApiResponse<User> userSambaApiResponse, Response response) {
        Toast.makeText(this.getActivity(), getString(R.string.user_save_done), Toast.LENGTH_SHORT).show();
        this.saveUserToPreferences();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        if (CONNECTIONS_TRIED < MAX_CONNECTIONS_TO_TRY){
            Toast.makeText(this.getActivity(), getString(R.string.user_save_failed_trying), Toast.LENGTH_SHORT).show();
            CONNECTIONS_TRIED++;
            mUserSambaApi.put(mUser, UserSettingsFragment.this);
        }else{
            Toast.makeText(this.getActivity(), getString(R.string.user_save_failed_try_later), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
