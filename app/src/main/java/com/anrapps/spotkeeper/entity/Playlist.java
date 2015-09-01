package com.anrapps.spotkeeper.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {

    public final String id;
    public final String href;
    public final String uri;

    public final String name;
    public final String ownerName;
    public final String imageUrl;

    private Playlist(Builder builder) {
        this.id = builder.id;
        this.href = builder.href;
        this.uri = builder.uri;
        this.name = builder.name;
        this.ownerName = builder.ownerName;
        this.imageUrl = builder.imageUrl;
    }

    protected Playlist(Parcel in) {
        id = in.readString();
        href = in.readString();
        uri = in.readString();
        name = in.readString();
        ownerName = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
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
        dest.writeString(ownerName);
        dest.writeString(imageUrl);
    }

    public static class Builder {

        private String id;
        private String href;
        private String uri;

        private String name;
        private String ownerName;
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

        public Builder setOwnerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Playlist build() {
            return new Playlist(this);
        }
    }
}
