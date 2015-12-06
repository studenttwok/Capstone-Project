package net.hklight.dmrmarc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HomeActivity extends AppCompatActivity {

    private final String LOG_TAG = HomeActivity.class.getSimpleName();

    private String mSearchKeyword = "";
    private boolean mShowFav = false;
    private ViewPager mViewPager;
    private HomePagerAdapter mHomePagerAdapter;
    private SearchView mSearchView;
    private MenuItem mSearchViewItem;

    final private android.support.v7.widget.SearchView.OnQueryTextListener queryListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {

            if (mSearchViewItem != null) {
                mSearchViewItem.collapseActionView();
            }
            Intent searchIntent = new Intent(HomeActivity.this, HomeActivity.class);
            searchIntent.putExtra("searchKeyword", query);
            startActivity(searchIntent);

            return true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // transection
//        Slide slide = new Slide(Gravity.TOP);
//        slide.addTarget(R.id.viewpager_home);
//        slide.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in));
//        getWindow().setEnterTransition(slide);


        // Load the ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // see if we have keyword
        if (getIntent().getStringExtra("searchKeyword") != null) {
            mSearchKeyword = getIntent().getStringExtra("searchKeyword");
        }

        mShowFav = getIntent().getBooleanExtra("showFav", false);


        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        final ActionBar actionBar = getSupportActionBar();

        // show the title
        if (mSearchKeyword.length() > 0) {
            actionBar.setTitle(String.format(getString(R.string.searchResultOf),mSearchKeyword));
        }
        if (mShowFav) {
            actionBar.setTitle(R.string.favouritedRepeaters);
        }


        mHomePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager_home);
        mViewPager.setAdapter(mHomePagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // add tabs to actionbar

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        // Add 3 tabs, specifying the tab's text and TabListener
        for (int i = 0; i < mHomePagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mHomePagerAdapter.getPageTitle(i)).setTabListener(tabListener));
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);
        try {
            mSearchViewItem = menu.findItem(R.id.action_search);
            mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
            mSearchView.setOnQueryTextListener(queryListener);


            if (mShowFav || mSearchKeyword.length() != 0) {
                mSearchViewItem.setVisible(false);
                menu.findItem(R.id.action_fav).setVisible(false);
            }


        } catch (Exception e) {}
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_fav) {
            // do a fav search
            Intent searchIntent = new Intent(HomeActivity.this, HomeActivity.class);
            searchIntent.putExtra("showFav", true);
            startActivity(searchIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class HomePagerAdapter extends FragmentStatePagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Bundle args = new Bundle();
            args.putString("searchKeyword", mSearchKeyword);
            args.putBoolean("showFav", mShowFav);

            Fragment fragment = null;
            if (i == 0) {
                fragment = new RepeaterMapFragment();
            } else if (i == 1) {
                fragment = new UserListFragment();
            }

            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            if (mShowFav) {
                // only repeater map is the interest
                return 1;
            }
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.repeater);
            } else if (position == 1) {
                return getString(R.string.user);
            }
            return "";
        }
    }

}
