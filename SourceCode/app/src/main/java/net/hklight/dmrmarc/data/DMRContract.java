package net.hklight.dmrmarc.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DMRContract {

    public static final String CONTENT_AUTHORITY = "net.hklight.dmrmarc";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USER = "user";
    public static final String PATH_REPEATER = "repeater";


    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static final String TABLE_NAME = "user";

        public static final String COLUMN_CALLSIGN = "callsign";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_HOME_RPTR = "home_rptr";
        public static final String COLUMN_REMARKS = "remarks";

        // some utility to generate the uri
        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUserSearchUri(String keyword) {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).appendQueryParameter("keyword", keyword).build();
        }

        // some utilites to parse the uri
        public static long getUserIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }




    }

    public static final class RepeaterEntity implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPEATER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPEATER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPEATER;

        public static final String TABLE_NAME = "repeater";

        public static final String COLUMN_CALLSIGN = "callsign";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_COLOR_CODE = "color_code";
        public static final String COLUMN_OFFSET = "offset";
        public static final String COLUMN_ASSIGNED = "assigned";
        public static final String COLUMN_TS_LINKED = "ts_linked";
        public static final String COLUMN_TRUSTEE = "trustee";
        public static final String COLUMN_IPSC_NETWORK = "ipsc_network";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_FAVOURITE = "favourite";


        // some utility to generate the uri
        public static Uri buildRepeaterUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRepeaterSearchUri(String keyword) {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPEATER).appendQueryParameter("keyword", keyword).build();
        }

        // some utilites to parse the uri
        public static long getRepeaterIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }


    }

}
