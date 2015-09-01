package com.anrapps.spotkeeper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anrapps.spotkeeper.R;
import com.anrapps.spotkeeper.entity.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private final List<Track> mTrackList = new ArrayList<>();
    private OnTrackClickListener mClickListener;

    public QueueAdapter() {}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Track track = mTrackList.get(position);
        holder.trackName.setText(track.name);
        holder.artistName.setText(new StringBuilder().append(track.artistName)
                .append(" - ")
                .append(track.albumName));

        Picasso.with(holder.albumCover.getContext())
                .load(track.albumImageUrl)
                .into(holder.albumCover);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mClickListener != null) mClickListener.OnTrackClicked(holder.view, track);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mTrackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView trackName;
        private final TextView artistName;
        private final ImageView albumCover;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            albumCover = (ImageView) itemView.findViewById(R.id.listitem_queue_album_cover);
            trackName = (TextView) itemView.findViewById(R.id.listitem_queue_name);
            artistName = (TextView) itemView.findViewById(R.id.listitem_queue_artist_name);
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