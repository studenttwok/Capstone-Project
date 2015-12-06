package net.hklight.dmrmarc;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.renderscript.Script;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.hklight.dmrmarc.data.DMRContract;

// Refer to sunshine Example
public class AppWidgetRemoteViewService extends RemoteViewsService {

    public static final String LOG_TAG = AppWidgetRemoteViewService.class.getSimpleName();

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



    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                Uri repeatersUri = DMRContract.RepeaterEntity.CONTENT_URI;

                String selection = DMRContract.RepeaterEntity.COLUMN_FAVOURITE + " = ? ";
                String[] args = new String[] {"1"};

                data = getContentResolver().query(repeatersUri,
                        REPEATER_COLUMNS,
                        selection,
                        args,
                        DMRContract.RepeaterEntity._ID + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                //Log.d(LOG_TAG, "getViewAt" + position);

                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget_main_item);
                long repeaterId = data.getLong(COL_ID);
                String repeaterCallsign = data.getString(COL_CALLSIGN);
                String repeaterFreq = data.getString(COL_FREQUENCY);
                String repeaterOffset = data.getString(COL_OFFSET);
                String ts = data.getString(COL_TS_LINKED);


                views.setTextViewText(R.id.textview_callsign, repeaterCallsign);
                views.setTextViewText(R.id.textview_frequency, repeaterFreq);
                views.setTextViewText(R.id.textview_offset, repeaterOffset);
                views.setTextViewText(R.id.textview_ts, ts);


                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("repeaterId", repeaterId);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.appwidget_main_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(COL_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
