package com.anrapps.spotkeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.anrapps.spotkeeper.adapter.ArtistAdapter;
import com.anrapps.spotkeeper.entity.Artist;
import com.anrapps.spotkeeper.util.PrefUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import android.app.LoaderManager;

import java.util.List;
import android.content.Loader;
import com.anrapps.spotkeeper.loader.DataLoader;
import android.widget.Toast;

public class ActivityMain extends BaseActivity implements ArtistAdapter.OnArtistClickListener, LoaderManager.LoaderCallbacks<List<Artist>> {

    private static final int REQUEST_AUTH = 1001;

    private ArtistAdapter mAdapter;
    private List<Artist> mArtists;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
		setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.navdrawer_item_artists);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
		
		mAdapter = new ArtistAdapter();
		mAdapter.setOnArtistClickListener(this);
		recyclerView.setAdapter(mAdapter);

        if (PrefUtils.isLastTokenExpired(this)) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                    getString(R.string.spotify_client_id),
                    AuthenticationResponse.Type.TOKEN,
                    getString(R.string.spotify_redirect_uri));
            builder.setScopes(new String[]{"user-follow-read", "streaming", "playlist-read-private", "playlist-read-collaborative"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_AUTH, request);
        } else {
            if (Application.getToken() == null)
                Application.setToken(PrefUtils.getUserAccessToken(this));
            getLoaderManager().initLoader(0, null, this);
        }
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_AUTH) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            AuthenticationResponse.Type type = response.getType();
            if (type == AuthenticationResponse.Type.TOKEN) {
                Application.setToken(response.getAccessToken());
                PrefUtils.setUserAccessToken(this, response.getAccessToken());
                PrefUtils.setLastTokenTime(this);
                getLoaderManager().initLoader(0, null, this);
            } else if (type == AuthenticationResponse.Type.EMPTY || type == AuthenticationResponse.Type.ERROR)
                finish();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ACTIVITY_ARTISTS;
    }

	@Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        return DataLoader.getInstanceForArtists(this, Application.getToken());
    }

    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
        if (data == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        else if (mArtists == null) {
                mArtists = data;
                mAdapter.setArtists(mArtists, false);
        }
    }


    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {}
	

    @Override
    public void OnArtistClicked(View view, Artist artist) {
        ActivityArtist.start(this, artist);
    }

    public static void launch(Activity from, boolean finishCurrent) {
        from.startActivity(new Intent(from, ActivityMain.class));
        if (finishCurrent) from.finish();
    }
}

