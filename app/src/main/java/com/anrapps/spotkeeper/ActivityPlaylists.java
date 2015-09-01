package com.anrapps.spotkeeper;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.anrapps.spotkeeper.adapter.PlaylistAdapter;
import com.anrapps.spotkeeper.entity.Playlist;
import com.anrapps.spotkeeper.loader.DataLoader;
import com.anrapps.spotkeeper.util.PrefUtils;

import java.util.List;

public class ActivityPlaylists extends BaseActivity implements PlaylistAdapter.OnPlaylistClickListener, LoaderManager.LoaderCallbacks<List<Playlist>> {

    private PlaylistAdapter mAdapter;
    private List<Playlist> mPlaylists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        mAdapter = new PlaylistAdapter();
        mAdapter.setOnPlaylistClickListener(this);
        recyclerView.setAdapter(mAdapter);

        final String userId = PrefUtils.getLoggedUserId(this);
        if (userId == null)
            getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<List<String>>() {
                @Override public Loader<List<String>> onCreateLoader(int id, Bundle args) {
                    return DataLoader.getInstanceForUserId(ActivityPlaylists.this, Application.getToken());
                }

                @Override public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
                    PrefUtils.setLoggedUserId(ActivityPlaylists.this, data.get(0));
                    getLoaderManager().initLoader(1, null, ActivityPlaylists.this);
                }

                @Override public void onLoaderReset(Loader<List<String>> loader) {

                }
            });
        else getLoaderManager().initLoader(0, null, ActivityPlaylists.this);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ACTIVITY_PLAYLISTS;
    }

    public static void launch(Activity from, boolean finishCurrent) {
        from.startActivity(new Intent(from, ActivityPlaylists.class));
        if (finishCurrent) from.finish();
    }

    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        return DataLoader.getInstanceForPlaylists(this, Application.getToken(), PrefUtils.getLoggedUserId(this));
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        if (data == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        else if (mPlaylists == null) {
            mPlaylists = data;
            mAdapter.setPlaylists(mPlaylists);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
    }

    @Override
    public void OnPlaylistClicked(View view, Playlist playlist) {
        ActivityAlbum.start(this, playlist);
    }

}
