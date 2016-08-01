package com.ribeiro.cardoso.rodasamba.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ribeiro.cardoso.rodasamba.util.Utility;
import com.ribeiro.cardoso.rodasamba.api.AgeGroupSambaApi;
import com.ribeiro.cardoso.rodasamba.api.RegionSambaApi;
import com.ribeiro.cardoso.rodasamba.api.SambaApi;
import com.ribeiro.cardoso.rodasamba.api.SexSambaApi;
import com.ribeiro.cardoso.rodasamba.data.Entities.AgeGroup;
import com.ribeiro.cardoso.rodasamba.data.Entities.Region;
import com.ribeiro.cardoso.rodasamba.data.Entities.Sex;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by diegopc86 on 19/08/14.
 */
public class SetupDatabaseService extends IntentService {

    private final static String LOG_TAG = SetupDatabaseService.class.getSimpleName();

    private RegionSambaApi mRegionApi;
    private AgeGroupSambaApi mAgeGroupSambaApi;
    private SexSambaApi mSexSambaApi;
    private SambaDbHelper mSambaHelper;

    public static final String SERVICE_STATUS = "service_status";
    public static final String STATUS = "status";
    public static final int SEVICE_FAIL = 0;
    public static final int SEVICE_SUCCESS = 1;

    protected boolean mAgeGroupDone = false;
    protected boolean mRegionDone = false;
    protected boolean mSexDone = false;



    public SetupDatabaseService() {
        super("SetupDatabaseService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mSambaHelper = SambaDbHelper.getInstance(this);

        mRegionApi = new RegionSambaApi();
        mRegionApi.index(new RegionCallbackHandler(this, mSambaHelper));

        mAgeGroupSambaApi = new AgeGroupSambaApi();
        mAgeGroupSambaApi.index(new AgeGroupCallbackHandler(this, mSambaHelper));

        mSexSambaApi = new SexSambaApi();
        mSexSambaApi.index(new SexCallbackHandler(this, mSambaHelper));

    }

    protected void isAllDatabasePopulated(){
        if (mSexDone && mAgeGroupDone && mRegionDone){
            Intent intent = new Intent();
            intent.setAction(SERVICE_STATUS);
            intent.putExtra(STATUS, SEVICE_SUCCESS);

            sendBroadcast(intent);
        }
    }

    private static class SexCallbackHandler implements Callback<SambaApi.SambaApiResponse<Sex>>{

        private final SetupDatabaseService mContext;
        private final SambaDbHelper mSambaHelper;

        public SexCallbackHandler(SetupDatabaseService mContext, SambaDbHelper mSambaDbHelper) {
            this.mContext = mContext;
            this.mSambaHelper = mSambaDbHelper;
        }


        @Override
        public void success(SambaApi.SambaApiResponse<Sex> sexSambaApiResponse, Response response) {
            if (sexSambaApiResponse.length > 0){
                final SQLiteDatabase writableDatabase = mSambaHelper.getWritableDatabase();

                try{
                    writableDatabase.beginTransaction();

                    for (Sex sex : sexSambaApiResponse.data){
                        ContentValues cv = new ContentValues(2);
                        cv.put(SambaContract.SexEntry.COLUMN_KEY, sex.key);
                        cv.put(SambaContract.SexEntry.COLUMN_NAME, sex.name);

                        writableDatabase.insert(
                                SambaContract.SexEntry.TABLE_NAME,
                                null,
                                cv);
                        Log.i(LOG_TAG, "Criando gênero: " + cv.getAsString("name"));
                    }
                    writableDatabase.setTransactionSuccessful();
                }catch (Exception e){
                    Log.e(LOG_TAG, "Error: " + e.getMessage());
                }finally {
                    writableDatabase.endTransaction();
                    mContext.mSexDone = true;
                    mContext.isAllDatabasePopulated();
                }
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(LOG_TAG, retrofitError.getUrl() + ": " + retrofitError.getMessage());
            Utility.forceFirstLaunch(mContext, true);
        }
    }

    private static class RegionCallbackHandler implements Callback<SambaApi.SambaApiResponse<Region>>{
        private final SambaDbHelper mSambaHelper;
        private final SetupDatabaseService mContext;

        public RegionCallbackHandler(SetupDatabaseService context, SambaDbHelper mSambaHelper) {
            this.mSambaHelper = mSambaHelper;
            this.mContext = context;
        }

        @Override
        public void success(SambaApi.SambaApiResponse<Region> regionSambaApiResponse, Response response) {

            if (regionSambaApiResponse.length > 0){
                final SQLiteDatabase writableDatabase = mSambaHelper.getWritableDatabase();

                try{
                    writableDatabase.beginTransaction();

                    for (Region region : regionSambaApiResponse.data){
                        ContentValues cv = new ContentValues(2);
                        cv.put(SambaContract.RegionEntry._ID, region.id);
                        cv.put(SambaContract.RegionEntry.COLUMN_NAME, region.name);

                        writableDatabase.insert(
                                SambaContract.RegionEntry.TABLE_NAME,
                                null,
                                cv);
                        Log.i(LOG_TAG, "Criando região: " + cv.getAsString("name"));
                    }
                    writableDatabase.setTransactionSuccessful();
                }catch (Exception e){
                    Log.e(LOG_TAG, "Error: " + e.getMessage());
                }finally {
                    writableDatabase.endTransaction();

                    mContext.mRegionDone = true;
                    mContext.isAllDatabasePopulated();
                }
            }

        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(LOG_TAG, retrofitError.getUrl() + ": " + retrofitError.getMessage());
            Utility.forceFirstLaunch(mContext, true);
        }
    }

    private static class AgeGroupCallbackHandler implements Callback<SambaApi.SambaApiResponse<AgeGroup>>{

        private final SambaDbHelper mSambaHelper;
        private final SetupDatabaseService mContext;

        public AgeGroupCallbackHandler(SetupDatabaseService context, SambaDbHelper sambaDbHelper){
            this.mSambaHelper = sambaDbHelper;
            this.mContext = context;
        }

        @Override
        public void success(SambaApi.SambaApiResponse<AgeGroup> ageGroupSambaApiResponse, Response response) {
            if (ageGroupSambaApiResponse.length > 0){
                final SQLiteDatabase writableDatabase = mSambaHelper.getWritableDatabase();

                try{
                    writableDatabase.beginTransaction();

                    for (AgeGroup ageGroup : ageGroupSambaApiResponse.data){
                        ContentValues cv = new ContentValues(2);
                        cv.put(SambaContract.AgeGroupEntry._ID, ageGroup.id);
                        cv.put(SambaContract.AgeGroupEntry.COLUMN_NAME, ageGroup.name);

                        writableDatabase.insert(
                                SambaContract.AgeGroupEntry.TABLE_NAME,
                                null,
                                cv);
                        Log.i(LOG_TAG, "Criando faixa etária: " + cv.getAsString("name"));
                    }
                    writableDatabase.setTransactionSuccessful();
                }catch (Exception e){
                    Log.e(LOG_TAG, "Error: " + e.getMessage());
                }
                finally {
                    writableDatabase.endTransaction();

                    mContext.mAgeGroupDone = true;
                    mContext.isAllDatabasePopulated();
                }
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(LOG_TAG, retrofitError.getUrl() + ": " + retrofitError.getMessage());
            Utility.forceFirstLaunch(mContext, true);
        }
    }
}
