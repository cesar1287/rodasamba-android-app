package com.ribeiro.cardoso.rodasamba.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by diegopc86 on 12/08/14.
 * Defines tables and columns names for the Samba database.
 */

public final class SambaContract {

    public static final String CONTENT_AUTHORITY = "com.ribeiro.cardoso.rodasamba";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_REGION = "region";
    public static final String PATH_AGE_GROUP = "age_group";
    public static final String PATH_USER = "user";
    public static final String PATH_EVENT = "event";
    public static final String PATH_SPECIAL_EVENT = "special_event";

    public static final class RegionEntry implements BaseColumns{

        /*
            Essas propriedades são para um uso posterior, caso queiramos implementar uma camada de ContentProvider

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REGION).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        */

        public static final String TABLE_NAME = "region";

        public static final String COLUMN_NAME = "name";
    }

    public static final class AgeGroupEntry implements BaseColumns{

        /*
            Essas propriedades são para um uso posterior, caso queiramos implementar uma camada de ContentProvider

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AGE_GROUP).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AGE_GROUP;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AGE_GROUP;

        */


        public static final String TABLE_NAME = "age_group";

        public static final String COLUMN_NAME =  "name";
    }

    public static final class SexEntry implements BaseColumns{
        public static final String TABLE_NAME = "sex";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
    }

    public static final class UserEntry implements BaseColumns{

        /*
            Essas propriedades são para um uso posterior, caso queiramos implementar uma camada de ContentProvider

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        */

        public static final String TABLE_NAME = "user";

        public static final String COLUMN_REGION_ID = "region_id";
        public static final String COLUMN_AGE_GROUP_ID = "age_group_id";
        public static final String COLUMN_SEX = "sex";

    }

    public static final class EventEntry implements BaseColumns{

        /*
            Essas propriedades são para um uso posterior, caso queiramos implementar uma camada de ContentProvider

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        public static Uri buildEventUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        */

        public static final String TABLE_NAME = "event";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_EVENT_DATE = "date";
        public static final String COLUMN_TICKET_PRICE = "ticket_price";
        public static final String COLUMN_DRINK_PRICE = "drink_price";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";


        public static final String COLUMN_REGION_ID = "region_id";
    }

    public static final class SpecialEventEntry implements BaseColumns{

        /*
            Essas propriedades são para um uso posterior, caso queiramos implementar uma camada de ContentProvider

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPECIAL_EVENT).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SPECIAL_EVENT;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SPECIAL_EVENT;


        public static Uri buildSpecialEventWithEvent(long event_id){
            return  CONTENT_URI.buildUpon().appendPath(String.valueOf(event_id)).build();
        }

        */


        public static final String TABLE_NAME = "special_event";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_EVENT_ID = "event_id";



    }

}
