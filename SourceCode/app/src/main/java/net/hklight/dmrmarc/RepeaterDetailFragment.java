package net.hklight.dmrmarc;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.hklight.dmrmarc.data.DMRContract;

public class RepeaterDetailFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, CompoundButton.OnCheckedChangeListener {

    private final String LOG_TAG = RepeaterDetailFragment.class.getSimpleName();

    private static final int REPEATER_LOADER = 0;


    private static final String[] REPEATER_COLUMNS = {
            DMRContract.RepeaterEntity._ID,
            DMRContract.RepeaterEntity.COLUMN_CALLSIGN,
            DMRContract.RepeaterEntity.COLUMN_CITY,
            DMRContract.RepeaterEntity.COLUMN_STATE,
            DMRContract.RepeaterEntity.COLUMN_COUNTRY,
            DMRContract.RepeaterEntity.COLUMN_FREQUENCY,
            DMRContract.RepeaterEntity.COLUMN_COLOR_CODE,
            DMRContract.RepeaterEntity.COLUMN_OFFSET,
            DMRContract.RepeaterEntity.COLUMN_ASSIGNED,
            DMRContract.RepeaterEntity.COLUMN_TS_LINKED,
            DMRContract.RepeaterEntity.COLUMN_TRUSTEE,
            DMRContract.RepeaterEntity.COLUMN_IPSC_NETWORK,
            DMRContract.RepeaterEntity.COLUMN_LAT,
            DMRContract.RepeaterEntity.COLUMN_LNG,
            DMRContract.RepeaterEntity.COLUMN_FAVOURITE
    };

    // These indices are tied to USER_COLUMNS.  If USER_COLUMNS changes, these  must change.
    static final int COL_ID = 0;
    static final int COL_CALLSIGN = 1;
    static final int COL_CITY = 2;
    static final int COL_STATE = 3;
    static final int COL_COUNTRY = 4;
    static final int COL_FREQUENCY = 5;
    static final int COL_COLOR_CODE = 6;
    static final int COL_OFFSET = 7;
    static final int COL_ASSIGNED = 8;
    static final int COL_TS_LINKED = 9;
    static final int COL_TRUSTEE = 10;
    static final int COL_IPSC_NETWORK = 11;
    static final int COL_LAT = 12;
    static final int COL_LNG = 13;
    static final int COL_FAVOURITE = 14;

    private long mRepeaterId = -1;

    private TextView mRepeaterIdTextView;
    private TextView mCallSignTextView;
    private TextView mCityTextView;
    private TextView mStateTextView;
    private TextView mCountryTextView;
    private TextView mFrequencyTextView;
    private TextView mColorCodeTextView;
    private TextView mOffsetTextView;
    private TextView mAssignedTextView;
    private TextView mTimeslotTextView;
    private TextView mTrusteeTextView;
    private TextView mIPSCTextView;
    private TextView mLatTextView;
    private TextView mLngTextView;
    private CheckBox mStarCheckBox;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL;
        int theme = 0;
        setStyle(style, theme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (!getShowsDialog()) {
            //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            View rootView = inflater.inflate(R.layout.fragment_repeaterdetail, null);
            setupUI(rootView);
            return rootView;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupUI(View rootView) {
        mRepeaterIdTextView = (TextView) rootView.findViewById(R.id.textview_id);
        mCallSignTextView = (TextView) rootView.findViewById(R.id.textview_callsign);
        mCityTextView = (TextView) rootView.findViewById(R.id.textview_city);
        mStateTextView = (TextView) rootView.findViewById(R.id.textview_state);
        mCountryTextView = (TextView) rootView.findViewById(R.id.textview_country);
        mFrequencyTextView = (TextView) rootView.findViewById(R.id.textview_frequency);
        mColorCodeTextView = (TextView) rootView.findViewById(R.id.textview_color_code);
        mOffsetTextView = (TextView) rootView.findViewById(R.id.textview_offset);
        mAssignedTextView = (TextView) rootView.findViewById(R.id.textview_assigned);
        mTimeslotTextView = (TextView) rootView.findViewById(R.id.textview_timesolt);
        mTrusteeTextView = (TextView) rootView.findViewById(R.id.textview_trustee);
        mIPSCTextView = (TextView) rootView.findViewById(R.id.textview_ipsc);
        mLatTextView = (TextView) rootView.findViewById(R.id.textview_lat);
        mLngTextView = (TextView) rootView.findViewById(R.id.textview_lng);
        mStarCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox_star);
        mStarCheckBox.setOnCheckedChangeListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mRepeaterId = getArguments().getLong("repeaterId");
        getLoaderManager().restartLoader(REPEATER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = DMRContract.RepeaterEntity._ID + " ASC";
        Uri repeaterUri = DMRContract.RepeaterEntity.buildRepeaterUri(mRepeaterId);

        return new CursorLoader(getActivity(),
                repeaterUri,
                REPEATER_COLUMNS,
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

        // fill data
        mRepeaterIdTextView.setText(data.getString(COL_ID));
        mCallSignTextView.setText(data.getString(COL_CALLSIGN));
        mCityTextView.setText(data.getString(COL_CITY));
        mStateTextView.setText(data.getString(COL_STATE));
        mCountryTextView.setText(data.getString(COL_COUNTRY));
        mFrequencyTextView.setText(data.getString(COL_FREQUENCY));
        mColorCodeTextView.setText(data.getString(COL_COLOR_CODE));
        mOffsetTextView.setText(data.getString(COL_OFFSET));
        mAssignedTextView.setText(data.getString(COL_ASSIGNED));
        mTimeslotTextView.setText(data.getString(COL_TS_LINKED));
        mTrusteeTextView.setText(data.getString(COL_TRUSTEE));
        mIPSCTextView.setText(data.getString(COL_IPSC_NETWORK));
        mLatTextView.setText(data.getString(COL_LAT));
        mLngTextView.setText(data.getString(COL_LNG));

        if (data.getInt(COL_FAVOURITE) == 0) {
            mStarCheckBox.setChecked(false);
        } else {
            mStarCheckBox.setChecked(true);
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_repeaterdetail, null);
        setupUI(rootView);
        setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.repeater_detail)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((FragmentAlertDialog)getActivity()).doPositiveClick();
                            }
                        }
                )
                .setView(rootView)
                .create();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // update database
        Uri repeaterUri = DMRContract.RepeaterEntity.CONTENT_URI;
        String where = DMRContract.RepeaterEntity._ID + " = ? ";
        String[] args = new String[] {mRepeaterId + ""};


        ContentValues cv = new ContentValues();
        if (isChecked) {
            cv.put(DMRContract.RepeaterEntity.COLUMN_FAVOURITE, 1);
        } else {
            cv.put(DMRContract.RepeaterEntity.COLUMN_FAVOURITE, 0);
        }

        int updateNum = getContext().getContentResolver().update(repeaterUri, cv, where, args);

        if (updateNum > 0) {
            // send broadcase to update widget, if any
            Intent broadcast = new Intent(this.getActivity(), AppWidgetProvider.class);
            broadcast.setAction(AppWidgetProvider.ACTION_UPDATE);
            getActivity().sendBroadcast(broadcast);

        }

        //Log.d(LOG_TAG, "UpdateNum:" + updateNum);
    }
}
