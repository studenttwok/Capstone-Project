package net.hklight.dmrmarc;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import net.hklight.dmrmarc.data.DMRContract;

public class RepeaterMapFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = RepeaterMapFragment.class.getSimpleName();

    private static final int REPEATER_LOADER = 0;

    private static final String[] REPEATER_COLUMNS = {
            DMRContract.RepeaterEntity._ID,
            DMRContract.RepeaterEntity.COLUMN_CALLSIGN,
            DMRContract.RepeaterEntity.COLUMN_LAT,
            DMRContract.RepeaterEntity.COLUMN_LNG,

            DMRContract.RepeaterEntity.COLUMN_FREQUENCY,
            DMRContract.RepeaterEntity.COLUMN_OFFSET,
            DMRContract.RepeaterEntity.COLUMN_TS_LINKED,
            DMRContract.RepeaterEntity.COLUMN_COLOR_CODE,

    };

    // These indices are tied to REPEATER_COLUMNS.  If REPEATER_COLUMNS changes, these  must change.
    static final int COL_ID = 0;
    static final int COL_CALLSIGN = 1;
    static final int COL_LAT = 2;
    static final int COL_LNG = 3;

    static final int COL_FREQUENCY = 4;
    static final int COL_OFFSET = 5;
    static final int COL_TS_LINKED = 6;
    static final int COL_COLOR_CODE = 7;

    private SupportMapFragment mapFragment;
    private ClusterManager<RepeaterItem> mClusterManager;
    private TextView fragmentDetailHintTextView;

    private ShareActionProvider myShareActionProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_repeatermap, null);
        //SupportMapFragment map = new SupportMapFragment();
        if (rootView.findViewById(R.id.textview_detailHint) != null) {
            fragmentDetailHintTextView = (TextView) rootView.findViewById(R.id.textview_detailHint);
        }
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady()");

        // Add a marker in Sydney, Australia, and move the camera.
        mClusterManager = new ClusterManager<RepeaterItem>(this.getActivity(), googleMap);


        ClusterRenderer cr = new DefaultClusterRenderer(getActivity(), googleMap, mClusterManager) {
            @Override
            protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
                super.onClusterItemRendered(clusterItem, marker);
                marker.setTitle(((RepeaterItem)clusterItem).getCallsign());
            }


        };

        mClusterManager.setRenderer(cr);

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<RepeaterItem>() {
            @Override
            public void onClusterItemInfoWindowClick(RepeaterItem repeaterItem) {
                long repeaterId = repeaterItem.getRepeaterId();

                // prepare data
                Bundle args = new Bundle();
                args.putLong("repeaterId", repeaterId);
                RepeaterDetailFragment dialog = new RepeaterDetailFragment();
                dialog.setArguments(args);


                boolean isInMasterDetailMode = getContext().getResources().getBoolean(R.bool.inMasterDetailMode);
                if (isInMasterDetailMode) {
                    // show in the right side
                    if (fragmentDetailHintTextView != null) {
                        fragmentDetailHintTextView.setVisibility(View.GONE);
                    }
                    FragmentTransaction rt = getChildFragmentManager().beginTransaction();
                    rt.replace(R.id.framelayout_repeaterdetail, dialog);
                    rt.commit();

                } else {
                    // show as a dialog fragment
                    dialog.show(getChildFragmentManager(), "dialog");
                }

            }
        });

        googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnInfoWindowClickListener(mClusterManager);

        // start the loader to get the data
        //mLoaderManager.initLoader(REPEATER_LOADER, getArguments(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getLoaderManager().restartLoader(0, getArguments(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fragmentDetailHintTextView!= null) {
            int visibility = fragmentDetailHintTextView.getVisibility();
            outState.putInt("fragmentHintTextViewVisibility", visibility);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && fragmentDetailHintTextView!= null) {
            //noinspection ResourceType
            fragmentDetailHintTextView.setVisibility(savedInstanceState.getInt("fragmentHintTextViewVisibility"));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader()");

        // Sort order:  Ascending, by date.
        String sortOrder = DMRContract.UserEntry._ID + " ASC";
        Uri repeaterUri = DMRContract.RepeaterEntity.CONTENT_URI;

        String where = null;
        String[] whereArgs = null;

        // show favourite
        if (args != null && args.containsKey("showFav") && args.getBoolean("showFav") == true) {
            where = DMRContract.RepeaterEntity.COLUMN_FAVOURITE + " = ? ";
            whereArgs = new String[] {"1"};

            Log.d(LOG_TAG, "show Fav");
        }
        // have searchKeyword
        if (args != null && args.containsKey("searchKeyword") && args.getString("searchKeyword").length() > 0) {
            String searchKeyword = args.getString("searchKeyword");

            long repeaterId = -1;
            try {
                repeaterId = Long.parseLong(searchKeyword);
                repeaterUri = DMRContract.RepeaterEntity.buildRepeaterUri(repeaterId);
            } catch (NumberFormatException ex) {
                // so treat it as keyword
                repeaterUri = DMRContract.RepeaterEntity.buildRepeaterSearchUri(searchKeyword);
            }
        }

        return new CursorLoader(getActivity(),
                repeaterUri,
                REPEATER_COLUMNS,
                where,
                whereArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        StringBuffer sb = new StringBuffer();

        boolean showFav = getArguments().getBoolean("showFav", false);

        // add data and draw the maper on map
        while (data.moveToNext()) {
            long repeaterId = data.getLong(COL_ID);
            double lat = data.getDouble(COL_LAT);
            double lng = data.getDouble(COL_LNG);
            String callsign = data.getString(COL_CALLSIGN);

            String frequency = data.getString(COL_FREQUENCY);
            String offset = data.getString(COL_OFFSET);
            String ts = data.getString(COL_TS_LINKED);
            String cc = data.getString(COL_COLOR_CODE);

            RepeaterItem offsetItem = new RepeaterItem(repeaterId, callsign, lat, lng);
            mClusterManager.addItem(offsetItem);

            //LatLng latLng = new LatLng(lat, lng);
            //mapFragment.getMap().addMarker(new MarkerOptions().position(latLng).title(callsign));
            //mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLng(latLng));
            if (showFav) {
                String url = "http://www.hamqth.com/" + callsign;
                sb.append(String.format(getString(R.string.shareLineFormat), repeaterId, callsign, frequency, offset, ts, cc, url));
            }

        }
        if (showFav) {
            shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.favouritedRepeaters));
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            shareIntent.setType("text/plain");
            myShareActionProvider.setShareIntent(shareIntent);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.repeatermapfragment, menu);

        if (getArguments().getBoolean("showFav")) {
            MenuItem shareItem = menu.findItem(R.id.action_share);
            shareItem.setVisible(true);

            myShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            Intent myShareIntent = new Intent(Intent.ACTION_SEND);
            myShareActionProvider.setShareIntent(myShareIntent);
        }
    }

    public class RepeaterItem implements ClusterItem {
        private final LatLng mPosition;
        private String mCallsign;
        private long mRepeaterId;

        public RepeaterItem(long repeaterId, String callsign, double lat, double lng) {
            mPosition = new LatLng(lat, lng);
            mCallsign = callsign;
            mRepeaterId = repeaterId;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        public String getCallsign() {
            return mCallsign;
        }

        public long getRepeaterId() { return mRepeaterId; }
    }

}
