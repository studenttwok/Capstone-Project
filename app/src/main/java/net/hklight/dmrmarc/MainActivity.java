package net.hklight.dmrmarc;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.hklight.dmrmarc.data.DMRDownloaderService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.BROADCAST_DOWNLOAD_SUCCESS.equals(action)) {
                // finish

                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                MainActivity.this.finish();

            } else if (Constant.BROADCAST_DOWNLOAD_FAIL.equals(action)) {
                // Toast the user..
                //Toast.makeText(MainActivity.this, R.string.noConnection, Toast.LENGTH_LONG).show();


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(R.string.app_name);
                alertDialog.setMessage(R.string.noConnection);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton(R.string.alert_dialog_ok,  new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alertDialog.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BROADCAST_DOWNLOAD_SUCCESS);
        intentFilter.addAction(Constant.BROADCAST_DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);

        // try fetch
        fetchDb();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void fetchDb() {
        Intent downloaderServiceIntent = new Intent(this, DMRDownloaderService.class);
        startService(downloaderServiceIntent);
    }
}
