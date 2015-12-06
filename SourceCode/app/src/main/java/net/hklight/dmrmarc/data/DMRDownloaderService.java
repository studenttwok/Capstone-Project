package net.hklight.dmrmarc.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.hklight.dmrmarc.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DMRDownloaderService extends IntentService {
    private final static String LOG_TAG = DMRDownloaderService.class.getSimpleName();

    public DMRDownloaderService() {
        super("DMRDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Download Task started");

        try {
            doDownload();
        } catch (IOException ex) {
            sendLocalBroadcast(Constant.BROADCAST_DOWNLOAD_FAIL);
        }

        // Finish
        Log.d(LOG_TAG, "Download Task finished");

    }

    private void doDownload() throws IOException {

        // get current time
        long currentTimeInMs = System.currentTimeMillis();
        long timeOutInMs = 60 * 60 * 24 * 3 * 1000;

        // That is user table
        // start the download process..
        String usersUrl = "http://www.dmr-marc.net/cgi-bin/trbo-database/datadump.cgi?table=users&format=csv";
        String repeatersUrl = "http://www.dmr-marc.net/cgi-bin/trbo-database/datadump.cgi?table=repeaters&format=csv";

        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        Request request = null;
        Response response = null;
        ArrayList<ContentValues> listToAded = new ArrayList<>();

        // Users table
        // Radio ID,Callsign,Name,City,State,Country,Home Repeater,Remarks
        //if (currentTimeInMs - PreferenceManager.getDefaultSharedPreferences(this).getLong(Constant.PREFERENCE_USERS_UPDATE_TS, 0) > timeOutInMs) {
        // No timeout, just download once...
        if (PreferenceManager.getDefaultSharedPreferences(this).getLong(Constant.PREFERENCE_USERS_UPDATE_TS, 0) == 0) {

            listToAded.clear();
            request = new Request.Builder().url(usersUrl).build();
            response = client.newCall(request).execute();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String eachLine = "";


            while ((eachLine = bufferedReader.readLine()) != null) {
                eachLine = eachLine.trim();
                eachLine = eachLine.replace("<br/>", "");
                eachLine = eachLine.replace("<BR/>", "");
                String parts[] = eachLine.split(",");
                //Log.d(LOG_TAG, parts[0]);

                if (parts.length != 8) {
                    continue;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(DMRContract.UserEntry._ID, Long.parseLong(parts[0]));
                contentValues.put(DMRContract.UserEntry.COLUMN_CALLSIGN, parts[1]);
                contentValues.put(DMRContract.UserEntry.COLUMN_NAME, parts[2]);
                contentValues.put(DMRContract.UserEntry.COLUMN_CITY, parts[3]);
                contentValues.put(DMRContract.UserEntry.COLUMN_STATE, parts[4]);
                contentValues.put(DMRContract.UserEntry.COLUMN_COUNTRY, parts[5]);
                contentValues.put(DMRContract.UserEntry.COLUMN_HOME_RPTR, parts[6]);
                contentValues.put(DMRContract.UserEntry.COLUMN_REMARKS, parts[7]);

                listToAded.add(contentValues);
            }

            // converted to array
            ContentValues[] results = new ContentValues[listToAded.size()];
            listToAded.toArray(results);


            // Uri
            Uri userUri = DMRContract.UserEntry.CONTENT_URI;

            // Remove all thing in table
            int deleteCount = getContentResolver().delete(userUri, null, null);

            // bulk insert
            int resultCount = getContentResolver().bulkInsert(userUri, results);

            if (resultCount > 0) {
                // update the database count
                PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(Constant.PREFERENCE_USERS_UPDATE_TS, currentTimeInMs).commit();
            }

            Log.d(LOG_TAG, resultCount + " Users inserted");


        }

        // Repeaters Table
        // Repeater ID,Callsign,City,State,Country,Frequency,Color Code,Offset,Assigned,TimeSlot,Trustee,IPSC,LAT,LNG
        //if (currentTimeInMs - PreferenceManager.getDefaultSharedPreferences(this).getLong(Constant.PREFERENCE_REPEATERS_UPDATE_TS, 0) > timeOutInMs) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getLong(Constant.PREFERENCE_REPEATERS_UPDATE_TS, 0) == 0) {

            // Uri
            Uri repeaterUri = DMRContract.RepeaterEntity.CONTENT_URI;

            // get the current favourite set
            ArrayList<Long> favouriteList = new ArrayList<>();
            Cursor cursor = getContentResolver().query(repeaterUri, new String[] {DMRContract.RepeaterEntity._ID}, DMRContract.RepeaterEntity.COLUMN_FAVOURITE + " = ?", new String[] {"1"}, null);
            while (cursor.moveToNext()) {
                long repeaterId = cursor.getLong(0);
                favouriteList.add(repeaterId);
            }
            cursor.close();


            listToAded.clear();
            request = new Request.Builder().url(repeatersUrl).build();
            response = client.newCall(request).execute();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String eachLine = "";
            while ((eachLine = bufferedReader.readLine()) != null) {
                eachLine = eachLine.trim();
                eachLine = eachLine.replace("<br/>", "");
                eachLine = eachLine.replace("<BR/>", "");
                String parts[] = eachLine.split(",");
                if (parts.length != 14) {
                    continue;
                }
                //Log.d(LOG_TAG, parts[0]);

                try {
                    long repeaterId = Long.parseLong(parts[0]);

                    int isFavourited = 0;
                    if (favouriteList.contains(repeaterId)) {
                        isFavourited = 1;
                    }

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DMRContract.RepeaterEntity._ID, repeaterId);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_CALLSIGN, parts[1]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_CITY, parts[2]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_STATE, parts[3]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_COUNTRY, parts[4]);

                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_FREQUENCY, Double.parseDouble(parts[5]));
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_COLOR_CODE, Integer.parseInt(parts[6]));
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_OFFSET, Double.parseDouble(parts[7]));

                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_ASSIGNED, parts[8]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_TS_LINKED, parts[9]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_TRUSTEE, parts[10]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_IPSC_NETWORK, parts[11]);
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_LAT, Double.parseDouble(parts[12]));
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_LNG, Double.parseDouble(parts[13]));
                    contentValues.put(DMRContract.RepeaterEntity.COLUMN_FAVOURITE, isFavourited);

                    listToAded.add(contentValues);

                } catch (NumberFormatException ex) {
                    //Log.d(LOG_TAG, "Error Record: " + parts[0]);
                }
            }

            // converted to array
            ContentValues[] results = new ContentValues[listToAded.size()];
            listToAded.toArray(results);



            // Remove everything in table
            int deleteCount = getContentResolver().delete(repeaterUri, null, null);

            // bulk insert
            int resultCount = getContentResolver().bulkInsert(repeaterUri, results);

            if (resultCount > 0) {
                // update the database count
                PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(Constant.PREFERENCE_REPEATERS_UPDATE_TS, currentTimeInMs).commit();
            }

            Log.d(LOG_TAG, resultCount + " Repeaters inserted");


        }

        sendLocalBroadcast(Constant.BROADCAST_DOWNLOAD_SUCCESS);
    }

    private void sendLocalBroadcast(String action) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}
