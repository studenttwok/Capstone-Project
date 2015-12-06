package net.hklight.dmrmarc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class RepeaterDetailActivity extends AppCompatActivity {

    private final String LOG_TAG = RepeaterDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_repeaterdetail);

        long repeaterId = getIntent().getLongExtra("repeaterId", -1);

        if (repeaterId == -1) {
            finish();
        }

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putLong("repeaterId", repeaterId);

            RepeaterDetailFragment repeaterDetailFragment = new RepeaterDetailFragment();
            repeaterDetailFragment.setArguments(args);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.framelayout_repeaterdetail, repeaterDetailFragment);
            ft.commit();

        }

    }


}
