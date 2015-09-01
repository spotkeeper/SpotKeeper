package com.anrapps.spotkeeper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anrapps.spotkeeper.R;
import com.anrapps.spotkeeper.entity.Playlist;
import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final List<Playlist> mPlaylistList = new ArrayList<>();
    private OnPlaylistClickListener mClickListener;

    public PlaylistAdapter() {}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Reuse artist list item layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_artist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Playlist playlist = mPlaylistList.get(position);
        holder.playlistName.setText(playlist.name);
        holder.playlistOwner.setText(playlist.ownerName);

        Picasso.with(holder.playlistImage.getContext()).load(playlist.imageUrl).into(holder.playlistImage,
                PicassoPalette.with(playlist.imageUrl, holder.playlistImage)
                        .use(PicassoPalette.Profile.MUTED_DARK)
                        .intoBackground(holder.detailContainer)
        );

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mClickListener != null) mClickListener.OnPlaylistClicked(holder.view, playlist);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPlaylistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView playlistName;
        private final TextView playlistOwner;
        private final ImageView playlistImage;
        private final View detailContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            playlistImage = (ImageView) view.findViewById(R.id.listitem_artist_image);
            playlistName = (TextView) view.findViewById(R.id.listitem_artist_name);
            playlistOwner = (TextView) view.findViewById(R.id.listitem_artist_genre);
            detailContainer = view.findViewById(R.id.listitem_artist_detail_container);
        }
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        mClickListener = listener;
    }

    public void setPlaylists(List<Playlist> playlists) {
        mPlaylistList.addAll(playlists);
        notifyDataSetChanged();
    }

    public interface OnPlaylistClickListener {
        void OnPlaylistClicked(View view, Playlist playlist);
    }
}

