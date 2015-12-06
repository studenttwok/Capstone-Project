package net.hklight.dmrmarc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DMRDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "dmr.db";

    public DMRDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + DMRContract.UserEntry.TABLE_NAME + " (" +

                DMRContract.UserEntry._ID + " INTEGER PRIMARY KEY," +

                // the ID of the location entry associated with this weather data
                DMRContract.UserEntry.COLUMN_CALLSIGN + " TEXT NOT NULL, " +
                DMRContract.UserEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DMRContract.UserEntry.COLUMN_CITY + " TEXT, " +
                DMRContract.UserEntry.COLUMN_STATE + " TEXT," +
                DMRContract.UserEntry.COLUMN_COUNTRY + " TEXT," +
                DMRContract.UserEntry.COLUMN_HOME_RPTR + " TEXT," +
                DMRContract.UserEntry.COLUMN_REMARKS + " TEXT);";



        final String SQL_CREATE_REPEATER_TABLE = "CREATE TABLE " + DMRContract.RepeaterEntity.TABLE_NAME + " (" +

                DMRContract.RepeaterEntity._ID + " INTEGER PRIMARY KEY," +

                // the ID of the location entry associated with this weather data
                DMRContract.RepeaterEntity.COLUMN_CALLSIGN + " TEXT NOT NULL, " +
                DMRContract.RepeaterEntity.COLUMN_CITY + " TEXT, " +
                DMRContract.RepeaterEntity.COLUMN_STATE + " TEXT, " +
                DMRContract.RepeaterEntity.COLUMN_COUNTRY + " TEXT," +

                DMRContract.RepeaterEntity.COLUMN_FREQUENCY + " REAL NOT NULL," +

                DMRContract.RepeaterEntity.COLUMN_COLOR_CODE + " INTEGER NOT NULL," +

                DMRContract.RepeaterEntity.COLUMN_OFFSET + " REAL," +

                DMRContract.RepeaterEntity.COLUMN_ASSIGNED + " TEXT," +
                DMRContract.RepeaterEntity.COLUMN_TS_LINKED + " TEXT," +
                DMRContract.RepeaterEntity.COLUMN_TRUSTEE + " TEXT," +
                DMRContract.RepeaterEntity.COLUMN_IPSC_NETWORK + " TEXT," +

                DMRContract.RepeaterEntity.COLUMN_LAT + " REAL," +
                DMRContract.RepeaterEntity.COLUMN_LNG + " REAL," +
                DMRContract.RepeaterEntity.COLUMN_FAVOURITE + " INTEGER NOT NULL);";


        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_REPEATER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DMRContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DMRContract.RepeaterEntity.TABLE_NAME);
        onCreate(db);
    }
}
