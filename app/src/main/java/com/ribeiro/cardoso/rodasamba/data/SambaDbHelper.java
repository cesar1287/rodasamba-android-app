package com.ribeiro.cardoso.rodasamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

/**
 * Created by diegopc86 on 12/08/14.
 */
public class SambaDbHelper extends SQLiteOpenHelper{

    private Context mContext;

    private static final String DB_NAME = "roda_samba.db";
    private static final int DB_VERSION = 1;

    private static SambaDbHelper instance;

    public synchronized static SambaDbHelper getInstance(Context context){

        if (instance == null){
            instance = new SambaDbHelper(context);
            instance.mContext = context;
        }

        return instance;
    }

    private SambaDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_REGIONS_TABLE = "create table " + SambaContract.RegionEntry.TABLE_NAME + " " +
                "(" +
                "" + SambaContract.RegionEntry._ID + " integer not null primary key autoincrement, " +
                "" + SambaContract.RegionEntry.COLUMN_NAME + " varchar not null, " +
                //Whenever the app sync with the service, it will replace those entries with the same _ID,
                //to ensure that the app has the updated entries
                " unique (" + SambaContract.RegionEntry._ID + ") on conflict replace " +
                ");";

        final String SQL_CREATE_AGE_GROUPS_TABLE = "create table " + SambaContract.AgeGroupEntry.TABLE_NAME + " " +
                " (" +
                " " + SambaContract.AgeGroupEntry._ID + " integer not null primary key autoincrement, " +
                " " + SambaContract.AgeGroupEntry.COLUMN_NAME + " varchar not null," +
                //Whenever the app sync with the service, it will replace those entries with the same _ID,
                //to ensure that the app has the updated entries
                " unique (" + SambaContract.AgeGroupEntry._ID + ") on conflict replace " +
                " );";

        final String SQL_CREATE_SEX_TABLE = "create table " + SambaContract.SexEntry.TABLE_NAME + " " +
                " (" +
                " " + SambaContract.SexEntry._ID + " integer not null primary key autoincrement," +
                " " + SambaContract.SexEntry.COLUMN_KEY + " varchar(1) not null, " +
                " " + SambaContract.SexEntry.COLUMN_NAME + " varchar not null, " +
                //Whenever the app sync with the service, it will replace those entries with the same _ID,
                //to ensure that the app has the updated entries
                " unique (" + SambaContract.SexEntry.COLUMN_KEY + ") on conflict replace " +
                " );";

        final String SQL_CREATE_EVENTS_TABLE = "create table " + SambaContract.EventEntry.TABLE_NAME + " " +
                " (" +
                " " + SambaContract.EventEntry._ID + " integer not null," +
                " " + SambaContract.EventEntry.COLUMN_NAME + " varchar not null," +
                " " + SambaContract.EventEntry.COLUMN_ADDRESS + " varchar not null," +
                " " + SambaContract.EventEntry.COLUMN_EVENT_DATE + " date not null," +
                " " + SambaContract.EventEntry.COLUMN_URL + " varchar null," +
                " " + SambaContract.EventEntry.COLUMN_DESCRIPTION + " text null," +
                " " + SambaContract.EventEntry.COLUMN_REGION_ID + " integer not null," +
                " " + SambaContract.EventEntry.COLUMN_TIME + " time not null," +
                " " + SambaContract.EventEntry.COLUMN_DRINK_PRICE + " text null," +
                " " + SambaContract.EventEntry.COLUMN_TICKET_PRICE + " text null," +
                " " + SambaContract.EventEntry.COLUMN_THUMBNAIL_URL + " varchar null," +
                " " + SambaContract.EventEntry.COLUMN_LATITUDE + " real null," +
                " " + SambaContract.EventEntry.COLUMN_LONGITUDE + " real null," +


                //Whenever the app sync with the service, it will replace those entries with the same _ID,
                //to ensure that the app has the updated entries
                " primary key (" + SambaContract.EventEntry._ID + ") on conflict replace " +
                " foreign key(" + SambaContract.EventEntry.COLUMN_REGION_ID + ") references " + SambaContract.RegionEntry.TABLE_NAME + "(" + SambaContract.RegionEntry._ID + ")" +
                ");";

        final String SQL_CREATE_SPECIAL_EVENTS_TABLE = "create table " + SambaContract.SpecialEventEntry.TABLE_NAME + " " +
                " (" +
                " " + SambaContract.SpecialEventEntry._ID + " integer not null primary key autoincrement," +
                " " + SambaContract.SpecialEventEntry.COLUMN_NAME + " varchar not null," +
                " " + SambaContract.SpecialEventEntry.COLUMN_DESCRIPTION + " text null," +
                " " + SambaContract.SpecialEventEntry.COLUMN_DATE + " date not null," +
                " " + SambaContract.SpecialEventEntry.COLUMN_EVENT_ID + " integer not null," +
                " foreign key(" + SambaContract.SpecialEventEntry.COLUMN_EVENT_ID + ") references " + SambaContract.EventEntry.TABLE_NAME + "(" + SambaContract.EventEntry._ID + ")" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_AGE_GROUPS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REGIONS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SPECIAL_EVENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SEX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public void saveDbToSdCard(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hours = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second =  c.get(Calendar.SECOND);

        //String currentDBPath = "/data/com.ribeiro.cardoso.rodasamba/databases/roda_samba.md";

        String backUpSystemData = "myDatabase-" + year + "-" + month + "-" + day + "-" + hours + "-" + minute + "-" + second + ".sqlite";
        //File currentDB = new File(data, currentDBPath);
        File path = new File(sd + "/MyDatabase/Database/");
        if(!path.exists()){
            path.mkdirs();
        }
        File backupDB = new File(path, backUpSystemData);
        FileChannel src = null;
        try {
            src = new FileInputStream(mContext.getDatabasePath(DB_NAME)).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
