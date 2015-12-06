package net.hklight.dmrmarc;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.hklight.dmrmarc.data.DMRContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDetailActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<String>, LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback, View.OnClickListener {

    private final String LOG_TAG = UserDetailActivity.class.getSimpleName();

    private static final int USER_LOADER = 0;

    private static final String[] USER_COLUMNS = {
            DMRContract.UserEntry._ID,
            DMRContract.UserEntry.COLUMN_CALLSIGN,
            DMRContract.UserEntry.COLUMN_NAME,
            DMRContract.UserEntry.COLUMN_CITY,
            DMRContract.UserEntry.COLUMN_STATE,
            DMRContract.UserEntry.COLUMN_COUNTRY,
            DMRContract.UserEntry.COLUMN_HOME_RPTR,
            DMRContract.UserEntry.COLUMN_REMARKS,
    };

    // These indices are tied to USER_COLUMNS.  If USER_COLUMNS changes, these  must change.
    static final int COL_ID = 0;
    static final int COL_CALLSIGN = 1;
    static final int COL_NAME = 2;
    static final int COL_CITY = 3;
    static final int COL_STATE = 4;
    static final int COL_COUNTRY = 5;
    static final int COL_HOME_RPTR = 6;
    static final int COL_REMARKS = 7;

    private long mUserId = -1;
    private String mCallsign = "";

    private TextView mUserIdTextView;
    private TextView mUserNameTextView;
    private TextView mUserStateTextView;
    private TextView mUserCityTextView;
    private TextView mUserCountryTextView;
    private TextView mUserHomeRepeaterTextView;
    private TextView mUserRemarksTextView;
    private TextView mNetworkToShow;
    private ProgressBar mLoadingProgressBar;

    private SupportMapFragment mapFragment;

    private LinearLayout mUserDetailLinearLayout;

    private LatLng mLatLng;
    private ArrayList<HashMap<String, String>> mRecentActivities = new ArrayList<HashMap<String, String>>();

    private boolean mIsReceiverRegistered = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                boolean isNotNetwork = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (!isNotNetwork) {
                    // retry
                    getUserDetailFromWeb();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetail);


        mUserDetailLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_userdetail);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mUserId = getIntent().getLongExtra("userId", -1);
        mCallsign = getIntent().getStringExtra("callsign");
        if (mUserId == -1) {
            finish();
        }

        mUserIdTextView = (TextView) findViewById(R.id.textview_id);
        mUserNameTextView = (TextView) findViewById(R.id.textview_name);
        mUserStateTextView = (TextView) findViewById(R.id.textview_state);
        mUserCityTextView = (TextView) findViewById(R.id.textview_city);
        mUserCountryTextView = (TextView) findViewById(R.id.textview_country);
        mUserHomeRepeaterTextView = (TextView) findViewById(R.id.textview_homerepeater);
        mUserRemarksTextView = (TextView) findViewById(R.id.textview_remarks);
        mNetworkToShow = (TextView)findViewById(R.id.textview_networkToShow);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(mCallsign);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportLoaderManager().restartLoader(USER_LOADER, null, this);
    }



    private void getUserDetailFromWeb() {
        // submit the request
        String url = "https://www.hamqth.com/" + mCallsign;
        StringRequest logRequest = new StringRequest(url, this, this);
        RetryPolicy retryPolicy = new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 20000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                throw error;
            }
        };


        logRequest.setRetryPolicy(retryPolicy);
        ((MainApplication)getApplication()).getVolleyRequestQueue().add(logRequest);

        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mNetworkToShow.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = DMRContract.UserEntry._ID + " ASC";
        Uri userUri = DMRContract.UserEntry.buildUserUri(mUserId);

        return new CursorLoader(this,
                userUri,
                USER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToNext()) {
            updateUI(data);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void updateUI(Cursor data) {
        Log.d(LOG_TAG, data.getString(COL_CALLSIGN));


        getSupportActionBar().setTitle(data.getString(COL_CALLSIGN));

        // Fill data
        mUserIdTextView.setText(data.getString(COL_ID));
        mUserNameTextView.setText(data.getString(COL_NAME));
        mUserStateTextView.setText(data.getString(COL_STATE));
        mUserCityTextView.setText(data.getString(COL_CITY));
        mUserCountryTextView.setText(data.getString(COL_COUNTRY));
        mUserHomeRepeaterTextView.setText(data.getString(COL_HOME_RPTR));
        mUserRemarksTextView.setText(data.getString(COL_REMARKS));

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // User cann't connect to server
        error.printStackTrace();

        mLoadingProgressBar.setVisibility(View.GONE);
        mNetworkToShow.setVisibility(View.VISIBLE);
        // let user know that you can connect to internet to get the recently activity


        // try to setup an listen
        // if connection occurs again, retry
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
        mIsReceiverRegistered = true;
    }

    @Override
    public void onResponse(String response) {
        // unregister the receiver
        mNetworkToShow.setVisibility(View.GONE);
        if (mIsReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mIsReceiverRegistered = false;
        }

        // do regular expression

        // Get lat lng
        // <td>Latitude:</td><td><b>22.31</b></td>
        // <td>Longitude:</td><td><b>114.17</b></td>
        // <td class="infoDesc">Grid:</td><td id="grid">OL72BJ</td>

        Matcher positionMatcher = null;
        Pattern gridPattern = Pattern.compile("<td class=\"infoDesc\">Grid:</td><td id=\"grid\">(.+?)</td>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Pattern latPattern = Pattern.compile("<td>Latitude:</td><td><b>(.+?)</b></td>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Pattern lngPattern = Pattern.compile("<td>Longitude:</td><td><b>(.+?)</b></td>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

        positionMatcher = gridPattern.matcher(response);
        if (positionMatcher.find()) {

            // We may find the grid
            String gridStr = positionMatcher.group(1);
            mLatLng = Maidenhead.fromMaidenhead(gridStr);

            Log.d(LOG_TAG, "member with latlng :" + mLatLng);

        } else {
            // so user is not registered in HamQth
            // we get another detail from web
            double lat = 0.0d;
            double lng = 0.0d;

            positionMatcher = latPattern.matcher(response);
            if (positionMatcher.find()) {
                try {
                    lat = Double.parseDouble(positionMatcher.group(1));
                } catch (NumberFormatException ex) { }
            }
            positionMatcher = lngPattern.matcher(response);
            if (positionMatcher.find()) {
                try {
                    lng = Double.parseDouble(positionMatcher.group(1));
                } catch (NumberFormatException ex) { }
            }

            if  (lat != 0.0d && lng != 0.0d) {
                // so both have value..
                mLatLng = new LatLng(lat, lng);
            }
        }

        // Get log
        Pattern historyPattern = Pattern.compile("<table class=\"table table-condensed table\\-striped\">(.+?)</table>", Pattern.DOTALL|Pattern.MULTILINE);
        Matcher historyMatcher = historyPattern.matcher(response);

        Pattern contentPattern = Pattern.compile("<tr>.+?<td>(.+?)</td>.+?<td>(.+?)</td>.+?<td>(.+?)</td>.+?</tr>", Pattern.DOTALL|Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher contentMatcher = null;
        if (historyMatcher.find()) {
            String tableContent = historyMatcher.group(1);

            contentMatcher = contentPattern.matcher(tableContent);

            while (contentMatcher.find()) {
                String v1 = contentMatcher.group(1);
                String v2 = contentMatcher.group(2);
                String v3 = contentMatcher.group(3);

                v1 = v1.trim();
                v2 = v2.trim();
                v3 = v3.trim();

                Log.d(LOG_TAG, "v1:" + v1 + " - v2 : " + v2 + " - v3: " + v3);

                HashMap<String, String> entry = new HashMap<>();
                entry.put("v1", v1);
                entry.put("v2", v2);
                entry.put("v3", v3);

                mRecentActivities.add(entry);
            }
        }


        // now draw UI
        restoreUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("latLng", mLatLng);
        outState.putSerializable("recentActivities", mRecentActivities);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState!= null) {
            mLatLng = savedInstanceState.getParcelable("latLng");
            mRecentActivities = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("recentActivities");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // If map is ready
        // pin the location
        if (mLatLng == null) {
            // so no previous state
            getUserDetailFromWeb();
        } else {
            // restore from the previous state
            restoreUI();
        }
    }

    private void restoreUI(){
        // dismiss the loading processbar view
        mLoadingProgressBar.setVisibility(View.GONE);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(mCallsign);

        mapFragment.getMap().addMarker(markerOptions);
        mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 10));


        // recent activies
        if (mRecentActivities.size() > 0) {
            for (int i=0; i < mRecentActivities.size(); i++) {
                HashMap<String, String> entry = mRecentActivities.get(i);
                String v1 = entry.get("v1");
                String v2 = entry.get("v2");
                String v3 = entry.get("v3");

                View cardView = getLayoutInflater().inflate(R.layout.cardview_userdetail, null);

                TextView t1 = (TextView) cardView.findViewById(R.id.textview_t1);
                TextView t2 = (TextView) cardView.findViewById(R.id.textview_t2);
                TextView t3 = (TextView) cardView.findViewById(R.id.textview_t3);
                t1.setText(v1);
                t2.setText(v2);
                t3.setText(v3);

                mUserDetailLinearLayout.addView(cardView);
            }
        } else {
            // let use know that there is not activies
            TextView infoTextView = new TextView(this);
            infoTextView.setText(R.string.user_norecentactivity);
            mUserDetailLinearLayout.addView(infoTextView);
        }


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            String url = "http://www.hamqth.com/" + mCallsign;
            String content = mUserIdTextView.getText() + " : " + mCallsign + "\n" + mUserStateTextView.getText() + " "+mUserCityTextView.getText() + " " + mUserCountryTextView.getText() + "\n" + url;
            Intent myShareIntent = new Intent(Intent.ACTION_SEND);
            myShareIntent.putExtra(Intent.EXTRA_TITLE, mCallsign);
            myShareIntent.putExtra(Intent.EXTRA_TEXT, content);
            myShareIntent.setType("text/plain");
            startActivity(Intent.createChooser(myShareIntent, getString(R.string.shareWith)));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mIsReceiverRegistered = false;
        }
    }
}
