package com.anrapps.spotkeeper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anrapps.spotkeeper.R;
import com.anrapps.spotkeeper.entity.Album;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private final List<Album> mAlbumList = new ArrayList<>();
    private OnAlbumClickListener mClickListener;

    public AlbumAdapter() {}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Reuse R.layout.listitem_queue
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = mAlbumList.get(position);
        holder.albumName.setText(album.name);
        holder.artistName.setText(album.artistName);

        Picasso.with(holder.albumCover.getContext())
                .load(album.imageUrl)
                .into(holder.albumCover);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mClickListener != null) mClickListener.OnAlbumClicked(holder.view, album);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView albumName;
        private final TextView artistName;
        private final ImageView albumCover;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            albumCover = (ImageView) itemView.findViewById(R.id.listitem_queue_album_cover);
            albumName = (TextView) itemView.findViewById(R.id.listitem_queue_name);
            artistName = (TextView) itemView.findViewById(R.id.listitem_queue_artist_name);
        }
    }

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        mClickListener = listener;
    }

    public void setAlbums(List<Album> albums) {
        mAlbumList.addAll(albums);
        notifyDataSetChanged();
    }

    public interface OnAlbumClickListener {
        void OnAlbumClicked(View view, Album album);
    }
}

