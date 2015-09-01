package com.anrapps.spotkeeper.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {

    public final String id;
    public final String href;
    public final String uri;

    public final String name;
    public final String artistName;
    public final String imageUrl;

    private Album(Builder builder) {
        this.id = builder.id;
        this.href = builder.href;
        this.uri = builder.uri;
        this.name = builder.name;
        this.artistName = builder.artistName;
        this.imageUrl = builder.imageUrl;
    }

    private Album(Parcel in) {
        id = in.readString();
        href = in.readString();
        uri = in.readString();
        name = in.readString();
        artistName = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
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
        dest.writeString(imageUrl);
    }

    public static class Builder {

        private String id;
        private String href;
        private String uri;

        private String name;
        private String artistName;
        private String imageUrl;

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

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Album build() {
            return new Album(this);
        }
    }
}
