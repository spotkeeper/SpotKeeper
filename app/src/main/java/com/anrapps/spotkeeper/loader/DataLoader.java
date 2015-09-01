package com.anrapps.spotkeeper.loader;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.io.BufferedReader;
import java.net.URL;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.anrapps.spotkeeper.entity.Album;
import com.anrapps.spotkeeper.entity.Artist;
import com.anrapps.spotkeeper.entity.Playlist;
import com.anrapps.spotkeeper.entity.Track;

import javax.net.ssl.HttpsURLConnection;

public class DataLoader<T> extends AsyncTaskLoader<List<T>> {

    public static final int QUERY_ME = 0;
    public static final int QUERY_MY_ARTISTS = 1;
    public static final int QUERY_ARTIST_ALBUMS = 2;
    public static final int QUERY_ALBUM_TRACKS = 3;
    public static final int QUERY_PLAYLISTS = 4;
    public static final int QUERY_PLAYLIST_TRACKS = 5;
    public static final int QUERY_SEARCH_ARTIST = 6;


    private static final String BASE_URL = "https://api.spotify.com/v1/";
	private static final String[] URL_PATHS = new String[] {
            BASE_URL + "me",
		    BASE_URL + "me/following?type=artist",
            BASE_URL + "artists/%s/albums",
            BASE_URL + "albums/%s/tracks",
            BASE_URL + "users/%s/playlists",
            BASE_URL + "users/%s/playlists/%s/tracks",
            BASE_URL + "search?q=%s&type=artist"
	};
	
	private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
	
	private final int mQuery;
	private final String mOauthToken;

	private Artist mArtist;
    private Album mAlbum;
    private Playlist mPlaylist;
    private String mUserId;
    private String mSearchQuery;

    private DataLoader(Context context, int query, Artist artist, Album album, Playlist playlist, String oauthToken, String userId, String searchQuery) {
        super(context);
        mQuery = query;
        mOauthToken = oauthToken;
        if (mOauthToken == null)
            throw new IllegalArgumentException("Must pass BUNDLE_KEY_OAUTH_TOKEN in the bundle");
        if ((mQuery == QUERY_ARTIST_ALBUMS || mQuery == QUERY_ALBUM_TRACKS) && artist == null)
            throw new IllegalArgumentException("Must pass artist as param for its ID for this query");
        mArtist = artist;
        if (mQuery == QUERY_ALBUM_TRACKS && album == null)
            throw new IllegalArgumentException("Must pass album as param for its ID for this query");
        mAlbum = album;
        if (mQuery == QUERY_PLAYLISTS && userId == null)
            throw new IllegalArgumentException("Must pass userId as param for getting playlist for this query");
        mUserId = userId;
        if (mQuery == QUERY_PLAYLIST_TRACKS && playlist == null) {
            throw new IllegalArgumentException("Must pass playlist as param for this query");
        }
        mPlaylist = playlist;
        if (mQuery == QUERY_SEARCH_ARTIST && searchQuery == null) {
            throw new IllegalArgumentException("Must pass a search string as param for this query");
        }
        mSearchQuery = searchQuery;
    }

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad();
	}

	@Override
	public List<T> loadInBackground() {

        try {
            //Must-close objects
            InputStream inputStream = null;
            HttpsURLConnection connection = null;
            BufferedReader bufferedReader = null;
            try {
                String urlString;
                switch (mQuery) {
                    case QUERY_ME:
                        urlString = URL_PATHS[mQuery];
                        break;
                    case QUERY_MY_ARTISTS:
                        urlString = URL_PATHS[mQuery];
                        break;
                    case QUERY_ARTIST_ALBUMS:
                        urlString = String.format(URL_PATHS[mQuery], mArtist.id);
                        break;
                    case QUERY_ALBUM_TRACKS:
                        urlString = String.format(URL_PATHS[mQuery], mAlbum.id);
                        break;
                    case QUERY_PLAYLISTS:
                        urlString = String.format(URL_PATHS[mQuery], mUserId);
                        break;
                    case QUERY_PLAYLIST_TRACKS:
                        urlString = String.format(URL_PATHS[mQuery], mPlaylist.ownerName, mPlaylist.id);
                        break;
                    case QUERY_SEARCH_ARTIST:
                        urlString = String.format(URL_PATHS[mQuery], URLEncoder.encode(mSearchQuery, "UTF-8"));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown query id");
                }
                final URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
                connection.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + mOauthToken);
                connection.connect();

                final int responseCode = connection.getResponseCode();
                if (responseCode >= 400 && responseCode <= 499) {
                    Log.e(getClass().getName(), "Bad response code (" + responseCode + "): returning null");
                    return null; //Bad authentication
                }

                inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) buffer.append(line).append("\n");
                if (buffer.length() == 0)
                    throw new IllegalStateException("No response received for the query");
                final String result =  buffer.toString();
                switch (mQuery) {
                    case QUERY_ME:
                        //noinspection unchecked
                        return (List<T>) parseMeResponse(result);
                    case QUERY_MY_ARTISTS:
                        //noinspection unchecked
                        return (List<T>) parseArtistJson(result);
                    case QUERY_ARTIST_ALBUMS:
                        //noinspection unchecked
                        return (List<T>) parseAlbumJson(result, mArtist);
                    case QUERY_ALBUM_TRACKS:
                        //noinspection unchecked
                        return (List<T>) parseTracksJson(result, mArtist, mAlbum);
                    case QUERY_PLAYLISTS:
                        //noinspection unchecked
                        return (List<T>) parsePlaylistsJson(result);
                    case QUERY_PLAYLIST_TRACKS:
                        //noinspection unchecked
                        return (List<T>) parsePlaylistTracksJson(result);
                    case QUERY_SEARCH_ARTIST:
                        //noinspection unchecked
                        return (List<T>) parseArtistJson(result);
                    default:
                        throw new UnsupportedOperationException("Unknown query id");
                }
            } finally {
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
                if (bufferedReader != null) bufferedReader.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		cancelLoad();
	}

    public static DataLoader<Artist> getInstanceForArtists(Activity context, String oauthToken) {
        return new DataLoader<>(context, QUERY_MY_ARTISTS, null, null, null, oauthToken, null, null);
    }

    public static DataLoader<Playlist> getInstanceForPlaylists(Activity context, String oauthToken, String userId) {
        return new DataLoader<>(context, QUERY_PLAYLISTS, null, null, null, oauthToken, userId, null);
    }

    public static DataLoader<Album> getInstanceForAlbums(Activity context, String oauthToken, Artist artist) {
        return new DataLoader<>(context, QUERY_ARTIST_ALBUMS, artist, null, null, oauthToken, null, null);
    }

    public static DataLoader<Track> getInstanceForTracks(Context context, String oauthToken, Artist artist, Album album) {
        return new DataLoader<>(context, QUERY_ALBUM_TRACKS, artist, album, null, oauthToken, null, null);
    }

    public static DataLoader<Track> getInstanceForTracks(Context context, String oauthToken, Playlist playlist) {
        return new DataLoader<>(context, QUERY_PLAYLIST_TRACKS, null, null, playlist, oauthToken, null, null);
    }

    public static DataLoader<String> getInstanceForUserId(Activity context, String oauthToken) {
        return new DataLoader<>(context, QUERY_ME, null, null, null, oauthToken, null, null);
    }

    public static DataLoader<Artist> getInstanceForArtistSearch(Activity context, String oauthToken, String searchQuery) {
        return new DataLoader<>(context, QUERY_SEARCH_ARTIST, null, null, null, oauthToken, null, searchQuery);
    }

    private static List<Artist> parseArtistJson(String json) throws JSONException {
        List<Artist> artists = new ArrayList<>();
        final JSONObject artistsJsonObject = new JSONObject(json).getJSONObject("artists");
        Artist.Builder builder = new Artist.Builder();
        final JSONArray artistsJsonArray = artistsJsonObject.getJSONArray("items");
        JSONObject jsonArtist;
        final int artistsArraySize = artistsJsonArray.length();
        for (int i = 0; i < artistsArraySize; i++) {
            jsonArtist = artistsJsonArray.getJSONObject(i);
            builder.setId(jsonArtist.getString("id"));
            builder.setHref(jsonArtist.getString("href"));
            builder.setUri(jsonArtist.getString("uri"));
            builder.setName(jsonArtist.getString("name"));
            builder.setGenre(jsonArtist.getJSONArray("genres").optString(0)); //main genre
            JSONArray imagesJsonArray = jsonArtist.getJSONArray("images");
            if (imagesJsonArray.length() == 0) builder.setImageUrl(null);
            else builder.setImageUrl(imagesJsonArray.getJSONObject(imagesJsonArray.length() == 1 ? 0 : 1).getString("url"));
            builder.setPopularity(jsonArtist.getInt("popularity"));
            builder.setFollowerCount(jsonArtist.getJSONObject("followers").getInt("total"));
            artists.add(builder.build());
        }
        return artists;
    }

    private static List<Album> parseAlbumJson(String json, Artist artist) throws JSONException {
        List<Album> albums = new ArrayList<>();
        Album.Builder builder = new Album.Builder();
        final JSONArray artistsJsonArray = new JSONObject(json).getJSONArray("items");
        JSONObject jsonArtist;
        final int artistsArraySize = artistsJsonArray.length();
        for (int i = 0; i < artistsArraySize; i++) {
            jsonArtist = artistsJsonArray.getJSONObject(i);
            builder.setId(jsonArtist.getString("id"));
            builder.setHref(jsonArtist.getString("href"));
            builder.setUri(jsonArtist.getString("uri"));
            builder.setName(jsonArtist.getString("name"));
            builder.setArtistName(artist.name);
            builder.setImageUrl(jsonArtist.getJSONArray("images").getJSONObject(1).getString("url"));
            albums.add(builder.build());
        }
        return albums;
    }

    private static List<Track> parseTracksJson(String json, Artist artist, Album album) throws JSONException {
        List<Track> tracks = new ArrayList<>();
        Track.Builder builder = new Track.Builder();
        final JSONArray tracksJsonArray = new JSONObject(json).getJSONArray("items");
        JSONObject jsonTrack;
        final int tracksJsonArraySize = tracksJsonArray.length();
        for (int i = 0; i < tracksJsonArraySize; i++) {
            jsonTrack = tracksJsonArray.getJSONObject(i);
            builder.setId(jsonTrack.getString("id"));
            builder.setHref(jsonTrack.getString("href"));
            builder.setUri(jsonTrack.getString("uri"));
            builder.setName(jsonTrack.getString("name"));
            builder.setArtistName(artist.name);
            builder.setAlbumName(album.name);
            builder.setAlbumImageUrl(album.imageUrl);
            tracks.add(builder.build());
        }
        return tracks;
    }

    private static List<Playlist> parsePlaylistsJson(String json) throws JSONException {
        List<Playlist> playlists = new ArrayList<>();
        Playlist.Builder builder = new Playlist.Builder();
        final JSONArray playlistsJsonArray = new JSONObject(json).getJSONArray("items");
        JSONObject jsonPlaylist;
        final int playlistsJsonArraySize = playlistsJsonArray.length();
        for (int i = 0; i < playlistsJsonArraySize; i++) {
            jsonPlaylist = playlistsJsonArray.getJSONObject(i);
            builder.setId(jsonPlaylist.getString("id"));
            builder.setHref(jsonPlaylist.getString("href"));
            builder.setUri(jsonPlaylist.getString("uri"));
            builder.setName(jsonPlaylist.getString("name"));
            builder.setOwnerName(jsonPlaylist.getJSONObject("owner").getString("id"));
            final JSONArray imagesArray = jsonPlaylist.getJSONArray("images");
            int length = imagesArray.length();
            if (length != 0)
                builder.setImageUrl(imagesArray.getJSONObject(length == 3 ? 1 : 0).getString("url"));
            playlists.add(builder.build());
        }
        return playlists;
    }

    private static List<Track> parsePlaylistTracksJson(String json) throws JSONException {
        List<Track> tracks = new ArrayList<>();
        Track.Builder builder = new Track.Builder();
        final JSONArray tracksJsonArray = new JSONObject(json).getJSONArray("items");
        JSONObject jsonTrack;
        final int tracksJsonSize = tracksJsonArray.length();
        for (int i = 0; i < tracksJsonSize; i++) {
            jsonTrack = tracksJsonArray.getJSONObject(i).getJSONObject("track");
            builder.setId(jsonTrack.getString("id"));
            builder.setHref(jsonTrack.getString("href"));
            builder.setUri(jsonTrack.getString("uri"));
            builder.setName(jsonTrack.getString("name"));
            builder.setArtistName(jsonTrack.getJSONArray("artists").getJSONObject(0).getString("name"));
            builder.setAlbumName(jsonTrack.getJSONObject("album").getString("name"));
            final JSONArray imagesArray = jsonTrack.getJSONObject("album").getJSONArray("images");
            int length = imagesArray.length();
            if (length != 0)
                builder.setAlbumImageUrl(imagesArray.getJSONObject(length == 3 ? 1 : 0).getString("url"));
            tracks.add(builder.build());
        }
        return tracks;
    }

    private static List parseMeResponse(String json) throws JSONException {
        final String id = new JSONObject(json).getString("id");
        List<String> list = new ArrayList<>();
        list.add(id);
        return list;
    }


}
