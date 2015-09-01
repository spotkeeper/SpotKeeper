package com.anrapps.spotkeeper.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SimpleCursorAdapter;

import com.anrapps.spotkeeper.entity.Artist;

public class SuggestionAdapter extends SimpleCursorAdapter {

    public final Helper mHelper;

    public SuggestionAdapter(Context context) {
        super(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[] {Helper.DISPLAY_SUGGESTION},
                new int[] {android.R.id.text1},
                0);
        mHelper = new Helper(context);
    }

    public void changeQuery(String query) {
        Cursor oldCursor = swapCursor(mHelper.getSuggestions(query));
        if (oldCursor != null) oldCursor.close();
    }

    public void close() {
        mHelper.close();
    }

    @Override
    public CharSequence convertToString(@NonNull Cursor cursor) {
        final int indexColumnSuggestion = cursor.getColumnIndex(Helper.DISPLAY_SUGGESTION);
        return cursor.getString(indexColumnSuggestion);
    }

    public static class Helper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "suggestions.db";
        private static final String DATABASE_TABLE_SUGGESTIONS = "suggestions";
        private static final int DATABASE_VERSION = 1;

        private static final String _ID = "_id";
        private static final String DISPLAY_SUGGESTION = "display1";
        private static final String ARTIST_ID = "artistId";
        private static final String ARTIST_HREF = "artistHref";
        private static final String ARTIST_URI = "artistUri";
        private static final String ARTIST_NAME = "artistName";
        private static final String ARTIST_GENRE = "artistGenre";
        private static final String ARTIST_IMAGE_URL = "artistImageUrl";
        private static final String ARTIST_POPULARITY = "artistPopularity";
        private static final String ARTIST_FOLLOWER_COUNT = "artistFollowerCount";
        private static final String COMMA_SEP = ", ";
        private static final String FIELD_TYPE_TEXT = " TEXT";
        private static final String FIELD_TYPE_INTEGER = " INTEGER";

        private final SQLiteDatabase mDatabase;

        public Helper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mDatabase = getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(("CREATE TABLE " + DATABASE_TABLE_SUGGESTIONS + "(" +
                    _ID + FIELD_TYPE_INTEGER + " PRIMARY KEY" +
                    COMMA_SEP + DISPLAY_SUGGESTION + FIELD_TYPE_TEXT + " UNIQUE ON CONFLICT REPLACE" +
                    ",query TEXT" +
                    ",date LONG" +
                    COMMA_SEP + ARTIST_ID + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_HREF + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_URI + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_NAME + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_GENRE + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_IMAGE_URL + FIELD_TYPE_TEXT +
                    COMMA_SEP + ARTIST_POPULARITY + FIELD_TYPE_INTEGER +
                    COMMA_SEP + ARTIST_FOLLOWER_COUNT + FIELD_TYPE_INTEGER +
                    ");"));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SUGGESTIONS);
            onCreate(db);
        }

        public long insertSuggestion(Artist artist) {
            ContentValues values = new ContentValues();
            values.put(DISPLAY_SUGGESTION, artist.name);
            values.put("query", artist.name);
            values.put("date", System.currentTimeMillis());
            values.put(ARTIST_ID, artist.id);
            values.put(ARTIST_HREF, artist.href);
            values.put(ARTIST_URI, artist.uri);
            values.put(ARTIST_NAME, artist.name);
            values.put(ARTIST_GENRE, artist.genre);
            values.put(ARTIST_IMAGE_URL, artist.imageUrl);
            values.put(ARTIST_POPULARITY, artist.popularity);
            values.put(ARTIST_FOLLOWER_COUNT, artist.followerCount);
            return mDatabase.insert(DATABASE_TABLE_SUGGESTIONS, null, values);
        }

        public Cursor getSuggestions(String text) {
            return mDatabase.query(
                    DATABASE_TABLE_SUGGESTIONS,
                    new String[] {_ID, DISPLAY_SUGGESTION},
                    DISPLAY_SUGGESTION + " LIKE '" + text + "%'",
                    null,
                    null,
                    null,
                    "date DESC");
        }

        public Artist getArtistSuggestionBased(String suggestion) {
            final Cursor c = mDatabase.query(
                    DATABASE_TABLE_SUGGESTIONS,
                    new String[]{ARTIST_ID, ARTIST_HREF, ARTIST_URI, ARTIST_NAME, ARTIST_GENRE, ARTIST_IMAGE_URL, ARTIST_POPULARITY, ARTIST_POPULARITY, "date"},
                    DISPLAY_SUGGESTION + " = ?",
                    new String[]{suggestion},
                    null,
                    null,
                    "date DESC");
            if (!c.moveToFirst()) return null;
            final Artist artist =  new Artist.Builder()
                    .setId(c.getString(0))
                    .setHref(c.getString(1))
                    .setUri(c.getString(2))
                    .setName(c.getString(3))
                    .setGenre(c.getString(4))
                    .setImageUrl(c.getString(5))
                    .setPopularity(c.getInt(6))
                    .setFollowerCount(c.getInt(7))
                    .build();
            c.close();
            return artist;
        }

        public void close() {
            mDatabase.close();
        }

    }
}
