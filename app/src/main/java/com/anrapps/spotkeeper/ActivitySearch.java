package com.anrapps.spotkeeper;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.anrapps.spotkeeper.adapter.ArtistAdapter;
import com.anrapps.spotkeeper.adapter.SuggestionAdapter;
import com.anrapps.spotkeeper.entity.Artist;
import com.anrapps.spotkeeper.loader.DataLoader;

import java.util.List;

public class ActivitySearch extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Artist>>, ArtistAdapter.OnArtistClickListener{

    private SearchView mSearchView = null;
    private String mQuery = "";

    private int mLoaderCount;

    private ArtistAdapter mAdapter;
    private SuggestionAdapter mSuggestionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_coordinator_recycler_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.searchable_hint);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        mAdapter = new ArtistAdapter();
        mAdapter.setOnArtistClickListener(this);
        recyclerView.setAdapter(mAdapter);

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)){
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = query == null ? "" : query;
            mQuery = query;
            if (mSearchView != null) mSearchView.setQuery(query, false);
//                doMySearch(query);
                Toast.makeText(this, "OnCreate Query :" + query, Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)){
//            String query = intent.getStringExtra(SearchManager.QUERY);
//
//            if (query != null) {
////                doMySearch(query);
//                Toast.makeText(this, "OnNewIntent Query :" + query, Toast.LENGTH_SHORT).show();
//            }
//        }else if (intent.getAction() != null && intent.getAction().equals(ACTION_SUGGESTION_CLICKED)) {
//            Log.wtf(getClass().getName(), "Extra: " + intent.getStringExtra(SearchManager.QUERY));
//            Artist artist = new SuggestionAdapter(this).mHelper.getArtistSuggestionBased(intent.getStringExtra(SearchManager.QUERY));
////            Toast.makeText(this, "Suggestion Clicked Query :" + intent.getStringExtra(SearchManager.QUERY), Toast.LENGTH_SHORT).show();
//            ActivityArtist.start(this, artist);
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuggestionAdapter.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView = searchView;
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false); // Do not iconify the widget; expand it by default

        mSuggestionAdapter = new SuggestionAdapter(this);
        searchView.setSuggestionsAdapter(mSuggestionAdapter);
        mSuggestionAdapter.changeQuery("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView.clearFocus();
                mQuery = s;
                mLoaderCount++;
                getLoaderManager().initLoader(mLoaderCount, null, ActivitySearch.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSuggestionAdapter.changeQuery(s);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                finish();
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mSuggestionAdapter.getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex("display1");
                Artist artist = mSuggestionAdapter.mHelper.getArtistSuggestionBased(cursor.getString(indexColumnSuggestion));
                mSuggestionAdapter.mHelper.insertSuggestion(artist);
                ActivityArtist.start(ActivitySearch.this, artist);
                return true;
            }
        });
        if (!TextUtils.isEmpty(mQuery)) {
            searchView.setQuery(mQuery, false);
        }
        return true;
    }

    @Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        return DataLoader.getInstanceForArtistSearch(this, Application.getToken(), mQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
        if (data != null) mAdapter.setArtists(data, true);
        else Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {}

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, ActivitySearch.class));
    }

    @Override
    public void OnArtistClicked(View view, Artist artist) {
        mSuggestionAdapter.mHelper.insertSuggestion(artist);
        ActivityArtist.start(this, artist);
    }

//    public static class CustomSuggestionAdapter extends SimpleCursorAdapter {
//
//        private Context mContext;
//
//        public CustomSuggestionAdapter(Context context) {
//            super(context, android.R.layout.simple_list_item_1, null, new String[]{"display1", "display2"}, new int[] {android.R.id.text1}, 0);
//            mContext = context;
//        }
//
//        @Override
//        public CharSequence convertToString(@NonNull Cursor cursor) {
//            return cursor.getString(0); //First item in projection
//        }
//
//        public void changeQuery(String query) {
//            final Uri BASE_CONTENT_URI = Uri.parse("content://" + SearchSuggestionProvider.AUTHORITY);
//            final String TABLE_NAME = "suggestions";
//            final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
//
//            Cursor oldCursor = swapCursor(mContext.getContentResolver().query(
//                    CONTENT_URI,
//                    new String[]{"display1", "display2"},
//                    "display1" + " LIKE '"+ query +"%'",
//                    null,
//                    "date DESC"));
//            if (oldCursor != null) oldCursor.close();
//        }
//    }
}
