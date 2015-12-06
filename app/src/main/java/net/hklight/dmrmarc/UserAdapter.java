package net.hklight.dmrmarc;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserAdapter extends CursorAdapter {

    private TextView mIdTextView;
    private TextView mCallsignTextView;
    private TextView mNameTextView;

    public UserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_userlist_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        //TextView callSignTextView = (TextView)view.findViewById(R.id.textview_callsign);
        viewHolder.mIdTextView.setText(cursor.getString(UserListFragment.COL_ID));
        viewHolder.mCallsignTextView.setText(cursor.getString(UserListFragment.COL_CALLSIGN));
        viewHolder.mNameTextView.setText(cursor.getString(UserListFragment.COL_NAME));
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    class ViewHolder {
        TextView mIdTextView;
        TextView mCallsignTextView;
        TextView mNameTextView;

        public ViewHolder(View rootView) {
            mIdTextView = (TextView) rootView.findViewById(R.id.textview_id);
            mCallsignTextView = (TextView) rootView.findViewById(R.id.textview_callsign);
            mNameTextView = (TextView) rootView.findViewById(R.id.textview_name);
        }
    }

}
