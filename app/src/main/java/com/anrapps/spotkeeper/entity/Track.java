package com.anrapps.spotkeeper.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

    public final String id;
    public final String href;
    public final String uri;

    public final String name;
    public final String artistName, albumName;
    public final String albumImageUrl;
    public final int duration; //in milliseconds

    private Track(Builder builder) {
        this.id = builder.id;
        this.href = builder.href;
        this.uri = builder.uri;
        this.name = builder.name;
        this.artistName = builder.artistName;
        this.albumName = builder.albumName;
        this.albumImageUrl = builder.albumImageUrl;
        this.duration = builder.duration;
    }

    protected Track(Parcel in) {
        id = in.readString();
        href = in.readString();
        uri = in.readString();
        name = in.readString();
        artistName = in.readString();
        albumName = in.readString();
        albumImageUrl = in.readString();
        duration = in.readInt();
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(href);
        dest.writeString(uri);
        dest.writeString(name);
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeString(albumImageUrl);
        dest.writeInt(duration);
    }

    public static class Builder {

        private String id;
        private String href;
        private String uri;

        private String name;
        private String artistName, albumName;
        private String albumImageUrl;
        private int duration;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setHref(String href) {
            this.href = href;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setArtistName(String artistName) {
            this.artistName = artistName;
            return this;
        }

        public Builder setAlbumName(String albumName) {
            this.albumName = albumName;
            return this;
        }

        public Builder setAlbumImageUrl(String albumImageUrl) {
            this.albumImageUrl = albumImageUrl;
            return this;
        }

        public Builder setDuration(int durationMs) {
            this.duration = durationMs;
            return this;
        }

        public Track build() {
            return new Track(this);
        }
    }
}
