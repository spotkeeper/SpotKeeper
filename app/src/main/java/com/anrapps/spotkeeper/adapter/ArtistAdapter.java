package com.anrapps.spotkeeper.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anrapps.spotkeeper.R;
import com.anrapps.spotkeeper.entity.Artist;
import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private final List<Artist> mArtistList = new ArrayList<>();
    private OnArtistClickListener mClickListener;

    public ArtistAdapter() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_artist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Artist artist = mArtistList.get(position);
        holder.artistName.setText(artist.name);
        String genre = artist.genre;
        if (!TextUtils.isEmpty(genre))
            genre = String.valueOf(genre.charAt(0)).toUpperCase() + genre.subSequence(1, genre.length());
        holder.genre.setText(genre);

//        holder.artistImage.setImageURI(Uri.parse(artist.imageUrl));
        if (!TextUtils.isEmpty(artist.imageUrl))
            Picasso.with(holder.artistImage.getContext())
                    .load(artist.imageUrl)
                    .placeholder(R.color.listitem_artist_image_background)
                    .error(R.drawable.listitem_artist_placeholder)
                    .into(holder.artistImage, PicassoPalette.with(artist.imageUrl, holder.artistImage)
                                    .use(PicassoPalette.Profile.MUTED_DARK)
                                    .intoBackground(holder.detailContainer)
                    );
        else holder.artistImage.setImageResource(R.drawable.listitem_artist_placeholder);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mClickListener != null) mClickListener.OnArtistClicked(holder.view, artist);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mArtistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final ImageView artistImage;
        private final TextView artistName;
        private final TextView genre;
        private final View detailContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            artistImage = (ImageView) itemView.findViewById(R.id.listitem_artist_image);
            artistName = (TextView) itemView.findViewById(R.id.listitem_artist_name);
            genre = (TextView) itemView.findViewById(R.id.listitem_artist_genre);
            detailContainer = itemView.findViewById(R.id.listitem_artist_detail_container);
        }
    }

    public void setOnArtistClickListener(OnArtistClickListener listener) {
        mClickListener = listener;
    }

    public void setArtists(List<Artist> artists, boolean clear) {
        if (clear) mArtistList.clear();
        mArtistList.addAll(artists);
        notifyDataSetChanged();
    }

    public interface OnArtistClickListener {
        void OnArtistClicked(View view, Artist artist);
    }
}
