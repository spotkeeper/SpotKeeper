package com.anrapps.spotkeeper;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.anrapps.spotkeeper.adapter.AlbumAdapter;
import com.anrapps.spotkeeper.entity.Album;
import com.anrapps.spotkeeper.entity.Artist;
import com.anrapps.spotkeeper.loader.DataLoader;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivityArtist extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Album>>, AlbumAdapter.OnAlbumClickListener {

    private static final String BUNDLE_KEY_ARTIST = "extra_artist";

    private Artist mArtist;
    private AlbumAdapter mAdapter;
    private List<Album> mAlbumList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Intent intent = getIntent();
        mArtist = intent.getParcelableExtra(BUNDLE_KEY_ARTIST);

        if (mArtist == null) {
            finish();
            return;
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mArtist.name);

        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        final ImageView mainImage = (ImageView) findViewById(R.id.front_image);

        Picasso.with(this)
                .load(mArtist.imageUrl)
                .into(imageView);
        Picasso.with(this)
                .load(mArtist.imageUrl)
                .into(mainImage);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new AlbumAdapter();
        mAdapter.setOnAlbumClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

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
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {
        return DataLoader.getInstanceForAlbums(this, Application.getToken(), mArtist);
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
        if (data == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        else if (mAlbumList == null) {
            mAlbumList = data;
            mAdapter.setAlbums(mAlbumList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {}

    public static void start(Context from, Artist artist) {
        Intent intent = new Intent(from, ActivityArtist.class);
        intent.putExtra(BUNDLE_KEY_ARTIST, artist);
        from.startActivity(intent);
    }

    @Override
    public void OnAlbumClicked(View view, Album album) {
        ActivityAlbum.start(this, mArtist, album);
//        ServiceRecord.loadAlbum(this, mArtist, album);
    }
}
