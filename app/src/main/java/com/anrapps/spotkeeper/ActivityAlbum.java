package com.anrapps.spotkeeper;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.anrapps.spotkeeper.adapter.QueueAdapter;
import com.anrapps.spotkeeper.adapter.TrackAdapter;
import com.anrapps.spotkeeper.entity.Album;
import com.anrapps.spotkeeper.entity.Artist;
import com.anrapps.spotkeeper.entity.Playlist;
import com.anrapps.spotkeeper.entity.Track;
import com.anrapps.spotkeeper.loader.DataLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ActivityAlbum extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Track>>, QueueAdapter.OnTrackClickListener {

    private static final String BUNDLE_KEY_ARTIST = "extra_artist";
    private static final String BUNDLE_KEY_ALBUM = "extra_album";
    private static final String BUNDLE_KEY_PLAYLIST = "extra_playlist";

    private Artist mArtist;
    private Album mAlbum;
    private Playlist mPlaylist;

    private List<Track> mTrackList;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        mArtist = intent.getParcelableExtra(BUNDLE_KEY_ARTIST);
        mAlbum = intent.getParcelableExtra(BUNDLE_KEY_ALBUM);
        mPlaylist = intent.getParcelableExtra(BUNDLE_KEY_PLAYLIST);
        if (mPlaylist == null && (mAlbum == null || mArtist == null)) finish();

        if (mPlaylist != null) setUpPlaylist();
        else setUpAlbum();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (checkIfPermissionGranted())
                    ServiceRecord.loadTracks(ActivityAlbum.this, (ArrayList<Track>) mTrackList);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Track>> onCreateLoader(int id, Bundle args) {
        if (mPlaylist != null)
            return DataLoader.getInstanceForTracks(this, Application.getToken(), mPlaylist);
        return DataLoader.getInstanceForTracks(this, Application.getToken(), mArtist, mAlbum);
    }

    @Override
    public void onLoadFinished(Loader<List<Track>> loader, List<Track> data) {
        if (data != null ) {
            if (mTrackList != null) return;
            mTrackList = data;
            if (mPlaylist != null) {
                QueueAdapter queueAdapter = new QueueAdapter();
                queueAdapter.setTracks(data);
                mRecyclerView.setAdapter(queueAdapter);
            } else {
                TrackAdapter trackAdapter = new TrackAdapter(mAlbum);
                trackAdapter.setTracks(data);
                mRecyclerView.setAdapter(trackAdapter);
            }
        } else Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<List<Track>> loader) {}

    public static void start(Context from, Artist artist, Album album) {
        Intent intent = new Intent(from, ActivityAlbum.class);
        intent.putExtra(BUNDLE_KEY_ARTIST, artist);
        intent.putExtra(BUNDLE_KEY_ALBUM, album);
        from.startActivity(intent);
    }

    public static void start(Context from, Playlist playlist) {
        Intent intent = new Intent(from, ActivityAlbum.class);
        intent.putExtra(BUNDLE_KEY_PLAYLIST, playlist);
        from.startActivity(intent);
    }

    @Override
    public void OnTrackClicked(View view, Track track) {
        //ServiceRecord.loadAlbum(this, mArtist, album);
    }

    private void setUpPlaylist() {
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mPlaylist.name);
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Picasso.with(this).load(mPlaylist.imageUrl).into(imageView);
    }

    private void setUpAlbum() {
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mAlbum.name);
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Picasso.with(this).load(mAlbum.imageUrl).into(imageView);
    }

    private boolean checkIfPermissionGranted() {
        int res = checkCallingOrSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        boolean granted = res == PackageManager.PERMISSION_GRANTED;
        if (!granted) buildDialog();
        return granted;
    }

    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_system_required);
        builder.setMessage(R.string.dialog_message_system_required);
        builder.setPositiveButton(R.string.dialog_positive_system_required, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                new ActivitySettings.MoveToSystemTask(ActivityAlbum.this).execute();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_system_required, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}
