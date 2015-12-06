package net.hklight.dmrmarc;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MainApplication extends Application {

    private RequestQueue mRequestQueue;
    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);
    }

    public RequestQueue getVolleyRequestQueue() {
        return mRequestQueue;
    }
}
