package net.hklight.dmrmarc;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.hklight.dmrmarc.data.DMRContract;

public class UserListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private final String LOG_TAG = UserListFragment.class.getSimpleName();

    private static final int USER_LOADER = 0;

    private UserAdapter mUserAdapter;

    private static final String[] USER_COLUMNS = {
        DMRContract.UserEntry._ID,
        DMRContract.UserEntry.COLUMN_CALLSIGN,
        DMRContract.UserEntry.COLUMN_NAME,
    };

    // These indices are tied to USER_COLUMNS.  If USER_COLUMNS changes, these  must change.
    static final int COL_ID = 0;
    static final int COL_CALLSIGN = 1;
    static final int COL_NAME = 2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mUserAdapter = new UserAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_userlist, null);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_user);
        listView.setAdapter(mUserAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(USER_LOADER, getArguments(), this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = DMRContract.UserEntry._ID + " ASC";
        Uri userUri = DMRContract.UserEntry.CONTENT_URI;

        // have searchKeyword
        if (args.containsKey("searchKeyword") && args.getString("searchKeyword").length() > 0) {
            String searchKeyword = args.getString("searchKeyword");

            long userId = -1;
            try {
                userId = Long.parseLong(searchKeyword);
                userUri = DMRContract.UserEntry.buildUserUri(userId);
            } catch (NumberFormatException ex) {
                // so treat it as keyword
                userUri = DMRContract.UserEntry.buildUserSearchUri(searchKeyword);
            }
        }


        return new CursorLoader(getActivity(),
                userUri,
                USER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mUserAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUserAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor)mUserAdapter.getItem(position);
        long userId = cursor.getLong(COL_ID);
        String callsign = cursor.getString(COL_CALLSIGN);

        View transitionView = view.findViewById(R.id.textview_id);

        // send it to user detailActivity
        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this.getActivity(), transitionView, ViewCompat.getTransitionName(transitionView)).toBundle();

        Intent userDetailActivity = new Intent(getActivity(), UserDetailActivity.class);
        userDetailActivity.putExtra("userId", userId);
        userDetailActivity.putExtra("callsign", callsign);
        //startActivity(userDetailActivity);

        getActivity().startActivity(userDetailActivity, bundle);


    }
}
