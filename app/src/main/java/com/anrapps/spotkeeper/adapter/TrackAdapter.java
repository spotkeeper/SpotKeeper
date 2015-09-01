package com.anrapps.spotkeeper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anrapps.spotkeeper.R;
import com.anrapps.spotkeeper.entity.Album;
import com.anrapps.spotkeeper.entity.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_NORMAL = 1;

    private OnTrackClickListener mClickListener;

    private final Album mAlbum;
    private final List<Track> mTrackList = new ArrayList<>();

    public TrackAdapter(Album album) {
        this.mAlbum = album;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mTrackList.size() + 1; //Track list size plus the header
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_track_header, parent, false));
            case VIEW_TYPE_NORMAL:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_track, parent, false));
            default:
                throw new UnsupportedOperationException("View type not supported");
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.artistName.setText(mAlbum.artistName);
                headerViewHolder.albumName.setText(mAlbum.name);
                Picasso.with(headerViewHolder.albumCover.getContext())
                        .load(mAlbum.imageUrl)
                        .into(headerViewHolder.albumCover);
                break;
            case VIEW_TYPE_NORMAL:
                final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                itemViewHolder.trackName.setText(mTrackList.get(position - 1).name);
                itemViewHolder.trackNumber.setText(String.valueOf(position));
                break;
            default:
                throw new UnsupportedOperationException("Unknown view type");
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView artistName;
        private final TextView albumName;
        private final ImageView albumCover;

        public HeaderViewHolder(View headerView) {
            super(headerView);
            view = headerView;
            artistName = (TextView) view.findViewById(R.id.listitem_track_header_artist_name);
            albumName = (TextView) view.findViewById(R.id.listitem_track_header_album_name);
            albumCover = (ImageView) view.findViewById(R.id.listitem_track_header_album_cover);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView trackName;
        private final TextView trackNumber;

        public ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            trackName = (TextView) view.findViewById(R.id.listitem_track_name);
            trackNumber = (TextView) view.findViewById(R.id.listitem_track_number);
        }
    }

    public void setOnTrackClickListener(OnTrackClickListener listener) {
        mClickListener = listener;
    }

    public void setTracks(List<Track> tracks) {
        mTrackList.addAll(tracks);
        notifyDataSetChanged();
    }

    public interface OnTrackClickListener {
        void OnTrackClicked(View view, Track track);
    }
}