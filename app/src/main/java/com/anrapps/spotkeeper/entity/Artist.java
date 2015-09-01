package com.anrapps.spotkeeper.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {

    public final String id;
    public final String href;
    public final String uri;

    public final String name;
    public final String genre;
    public final String imageUrl;

    public final int popularity;
    public final int followerCount;

    private Artist(Builder builder) {
        this.id = builder.id;
        this.href = builder.href;
        this.uri = builder.uri;
        this.name = builder.name;
        this.genre = builder.genre;
        this.imageUrl = builder.imageUrl;
        this.popularity = builder.popularity;
        this.followerCount = builder.followerCount;
    }

    private Artist(Parcel in) {
        this.id = in.readString();
        this.href = in.readString();
        this.uri = in.readString();
        this.name = in.readString();
        this.genre = in.readString();
        this.imageUrl = in.readString();
        this.popularity = in.readInt();
        this.followerCount = in.readInt();
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
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
        dest.writeString(genre);
        dest.writeString(imageUrl);
        dest.writeInt(popularity);
        dest.writeInt(followerCount);
    }

    public static class Builder {

        private String id;
        private String href;
        private String uri;

        private String name;
        private String genre;
        private String imageUrl;

        private int popularity;
        private int followerCount;

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

        public Builder setGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setPopularity(int popularity) {
            this.popularity = popularity;
            return this;
        }

        public Builder setFollowerCount(int followerCount) {
            this.followerCount = followerCount;
            return this;
        }

        public Artist build() {
            return new Artist(this);
        }
    }
}
