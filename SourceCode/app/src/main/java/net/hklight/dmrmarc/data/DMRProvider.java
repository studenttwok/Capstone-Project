package net.hklight.dmrmarc.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class DMRProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DMRDbHelper mOpenHelper;

    static final int USER = 100;
    static final int USER_WITH_ID = 101;
    static final int REPEATER = 200;
    static final int REPEATER_WITH_ID = 201;


    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DMRContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DMRContract.PATH_USER, USER);
        matcher.addURI(authority, DMRContract.PATH_USER + "/#", USER_WITH_ID);

        matcher.addURI(authority, DMRContract.PATH_REPEATER, REPEATER);
        matcher.addURI(authority, DMRContract.PATH_REPEATER + "/#", REPEATER_WITH_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new DMRDbHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case USER:
                return DMRContract.UserEntry.CONTENT_TYPE;
            case USER_WITH_ID:
                return DMRContract.UserEntry.CONTENT_ITEM_TYPE;
            case REPEATER:
                return DMRContract.RepeaterEntity.CONTENT_TYPE;
            case REPEATER_WITH_ID:
                return DMRContract.RepeaterEntity.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        String keyword = null;

        switch(sUriMatcher.match(uri)) {
            case USER:
                keyword = uri.getQueryParameter("keyword");

                if (keyword != null) {
                    // something to search
                    String mSelection = DMRContract.UserEntry.COLUMN_CALLSIGN + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_COUNTRY + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_CITY + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_HOME_RPTR + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_NAME + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_REMARKS + " LIKE ? OR " +
                            DMRContract.UserEntry.COLUMN_STATE + " LIKE ? ";

                    keyword = '%' + keyword + '%';

                    String[] mSelectionArgs = new String[] {
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword
                    };

                    retCursor = mOpenHelper.getReadableDatabase().query(
                            DMRContract.UserEntry.TABLE_NAME,
                            projection,
                            mSelection,
                            mSelectionArgs,
                            null,
                            null,
                            sortOrder
                    );

                } else {
                    // normal query
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            DMRContract.UserEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                }
                break;
            case USER_WITH_ID:
                retCursor = getUserByUserId(uri, projection, sortOrder);
                break;
            case REPEATER:
                keyword = uri.getQueryParameter("keyword");

                if (keyword != null) {
                    // something to search
                    String mSelection = DMRContract.RepeaterEntity.COLUMN_ASSIGNED + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_CALLSIGN + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_CITY + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_COLOR_CODE + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_COUNTRY + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_IPSC_NETWORK + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_TRUSTEE + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_TS_LINKED + " LIKE ? OR " +
                            DMRContract.RepeaterEntity.COLUMN_STATE + " LIKE ?";

                    keyword = '%' + keyword + '%';
                    String[] mSelectionArgs = new String[] {
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            keyword
                    };

                    retCursor = mOpenHelper.getReadableDatabase().query(
                            DMRContract.RepeaterEntity.TABLE_NAME,
                            projection,
                            mSelection,
                            mSelectionArgs,
                            null,
                            null,
                            sortOrder
                    );

                } else {

                    retCursor = mOpenHelper.getReadableDatabase().query(
                            DMRContract.RepeaterEntity.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                }
                break;
            case REPEATER_WITH_ID:
                retCursor = getRepeaterByRepeaterId(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // So we listen for this uri changes
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;

        switch(sUriMatcher.match(uri)) {
            case USER:
                deleteCount = mOpenHelper.getReadableDatabase().delete(
                        DMRContract.UserEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case REPEATER:
                deleteCount = mOpenHelper.getReadableDatabase().delete(
                        DMRContract.RepeaterEntity.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // So we listen for this uri changes
        if (deleteCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount = 0;
        switch (match) {
            case USER:
                returnCount = mOpenHelper.getReadableDatabase().update(
                        DMRContract.UserEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
            case REPEATER:
                returnCount = mOpenHelper.getReadableDatabase().update(
                        DMRContract.RepeaterEntity.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (returnCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {

            case USER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DMRContract.UserEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case REPEATER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DMRContract.RepeaterEntity.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getUserByUserId(Uri uri, String[] projection, String sortOrder) {
        long userId = DMRContract.UserEntry.getUserIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                DMRContract.UserEntry.TABLE_NAME,
                projection,
                DMRContract.UserEntry._ID + "=?",
                new String[] {userId + ""},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRepeaterByRepeaterId(Uri uri, String[] projection, String sortOrder) {
        long repeaterId = DMRContract.RepeaterEntity.getRepeaterIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                DMRContract.RepeaterEntity.TABLE_NAME,
                projection,
                DMRContract.RepeaterEntity._ID + "=?",
                new String[] {repeaterId + ""},
                null,
                null,
                sortOrder
        );
    }
}
