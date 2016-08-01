package com.ribeiro.cardoso.rodasamba.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.ribeiro.cardoso.rodasamba.data.SambaContract;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by diegopc86 on 14/08/14.
 */
public class SetupDatabaseTask extends AsyncTask<Void, Void, Void> {

    private final static String LOG_TAG = SetupDatabaseTask.class.getSimpleName();

    private Context mContext;

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    public SetupDatabaseTask(Context context) {
        super();

        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SambaDbHelper sambaDbHelper = SambaDbHelper.getInstance(mContext.getApplicationContext());
        final SQLiteDatabase writableDatabase = sambaDbHelper.getWritableDatabase();

        try {
            final Vector<ContentValues> contentValuesVector = parseJSON();

            if (contentValuesVector != null){

                writableDatabase.beginTransaction();

                for (int i = 0; i < contentValuesVector.size(); i++) {
                    ContentValues cv = contentValuesVector.get(i);

                    writableDatabase.insert(
                            SambaContract.RegionEntry.TABLE_NAME,
                            null,
                            cv);
                    Log.i(LOG_TAG, "Criando regiao: " + cv.getAsString("name"));
                }

                writableDatabase.setTransactionSuccessful();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            writableDatabase.endTransaction();
        }

        return null;
    }

    private Vector<ContentValues> parseJSON() throws JSONException {
        BufferedReader reader = null;
        String json;

        Vector<ContentValues> cvvector = new Vector<ContentValues>();

        try {
            final InputStream inputStream = mContext.getAssets().open("regions.json");

            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            json = buffer.toString();

            createContentValues(json, cvvector);


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cvvector;
    }

    private void createContentValues(String json, Vector<ContentValues> cvvector) throws JSONException {
        String REGION_ID = "region_id";
        String NAME = "name";

        JSONArray regionArray = new JSONArray(json);

        for (int i = 0; i < regionArray.length(); i++) {
            JSONObject region = regionArray.getJSONObject(i);
            ContentValues cv = new ContentValues(2);
            cv.put(SambaContract.RegionEntry._ID, region.getInt(REGION_ID));
            cv.put(SambaContract.RegionEntry.COLUMN_NAME, region.getString(NAME));

            cvvector.add(cv);
        }
    }
}
