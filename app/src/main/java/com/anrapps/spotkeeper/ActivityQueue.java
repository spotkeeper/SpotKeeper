package com.anrapps.spotkeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.anrapps.spotkeeper.adapter.QueueAdapter;
import com.anrapps.spotkeeper.widget.EmptyRecyclerView;

public class ActivityQueue extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_coordinator_recycler_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final EmptyRecyclerView recyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(findViewById(R.id.empty_view));
        QueueAdapter adapter = new QueueAdapter();
        recyclerView.setAdapter(adapter);

        ServiceRecord serviceRecord = ServiceRecord.getInstance();
        if (serviceRecord != null) adapter.setTracks(serviceRecord.getTrackList());

//        mAdapter = new ArtistAdapter();
//        mAdapter.setOnArtistClickListener(this);
    }


    public static void launch(Activity from, boolean finishCurrent) {
        from.startActivity(new Intent(from, ActivityQueue.class));
        if (finishCurrent) from.finish();
    }
}
