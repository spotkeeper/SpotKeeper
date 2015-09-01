package com.anrapps.spotkeeper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.anrapps.spotkeeper.util.PrefUtils;

public abstract class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_ACTIVITY_ARTISTS = R.id.menu_drawer_artists;      //ARTISTS
    protected static final int NAVDRAWER_ITEM_ACTIVITY_PLAYLISTS = R.id.menu_drawer_playlists;  //PLAYLISTS
    protected static final int NAVDRAWER_ITEM_QUEUE = R.id.menu_drawer_queue;                   //QUEUE
    protected static final int NAVDRAWER_ITEM_SETTINGS = R.id.menu_drawer_settings;             //SETTINGS

    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private Toolbar mActionBarToolbar;
    private DrawerLayout mDrawerLayout;

    private Handler mHandler;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        trySetupNavDrawer();

        View mainContent = findViewById(R.id.recycler_view);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }

        if (PrefUtils.isFirstTimeInit(this)) {
            PrefUtils.setNotFirstTimeInit(this);
            int res = checkCallingOrSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
            boolean granted = res == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_title_system_required);
                builder.setMessage(R.string.dialog_message_system_required);
                builder.setPositiveButton(R.string.dialog_positive_system_required, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        new ActivitySettings.MoveToSystemTask(BaseActivity.this).execute();
                    }
                });
                builder.setNegativeButton(R.string.dialog_negative_system_required, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_search:
                ActivitySearch.launch(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) closeNavDrawer();
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        final int itemId = menuItem.getItemId();
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (isSpecialItem(itemId)) goToNavDrawerItem(itemId);
        else {
            mHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            View mainContent = findViewById(R.id.recycler_view);
            if (mainContent != null)
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    protected abstract int getSelfNavDrawerItem();

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mActionBarToolbar != null)
                setSupportActionBar(mActionBarToolbar);
        }
        return mActionBarToolbar;
    }

    private void trySetupNavDrawer() {
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) return;

        NavigationView navigationView = (NavigationView) mDrawerLayout.findViewById(R.id.navigation_view);

        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            if (navigationView != null) ((ViewGroup) navigationView.getParent()).removeView(navigationView);
            mDrawerLayout = null;
            return;
        }

        if (navigationView == null) return;


//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            final int navHeaderHeight = getResources().getDimensionPixelSize(
//                    R.dimen.navdrawer_header_height);
//
//            final ViewGroup navHeader = (ViewGroup) navigationView.findViewById(R.id.navdrawer_header);
//            final View navHeaderContent = navHeader.findViewById(R.id.navdrawer_header_content);
//            navHeaderContent.setPadding(navHeaderContent.getPaddingLeft(),
//                    navHeaderContent.getPaddingTop() + UIUtils.getStatusBarHeight(this),
//                    navHeaderContent.getPaddingRight(),
//                    navHeaderContent.getPaddingBottom());
//            ViewGroup.LayoutParams navHeaderLP = navHeader.getLayoutParams();
//            navHeaderLP.height = navHeaderHeight + UIUtils.getStatusBarHeight(this);
//            navHeader.setLayoutParams(navHeaderLP);
//        }
        navigationView.setNavigationItemSelectedListener(this);

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
        navigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
    }

    private void goToNavDrawerItem(int item) {
        switch (item) {
            case NAVDRAWER_ITEM_ACTIVITY_ARTISTS:
                ActivityMain.launch(this, true);
                break;
            case NAVDRAWER_ITEM_ACTIVITY_PLAYLISTS:
                ActivityPlaylists.launch(this, true);
                break;
            case NAVDRAWER_ITEM_QUEUE:
                ActivityQueue.launch(this, false);
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                ActivitySettings.launch(this, false);
                break;
//            case R.id.menu_drawer_settings:
//                intent = new Intent(this, BrowseSessionsActivity.class);
//                startActivity(intent);
//                finish();
//                break;
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isSpecialItem(final int itemId) {
        if (itemId == NAVDRAWER_ITEM_QUEUE) return true;
        if (itemId == NAVDRAWER_ITEM_SETTINGS) return true;
        return false;
    }

}