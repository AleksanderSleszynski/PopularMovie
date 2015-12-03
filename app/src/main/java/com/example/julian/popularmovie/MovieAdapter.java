package com.example.julian.popularmovie;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.julian.popularmovie.activity.MainActivityFragment;
import com.example.julian.popularmovie.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private RecyclerView mRecyclerView;
    private final Drawable mNoImageDrawable;
    private ArrayList<Movie> mMovies;
    private LayoutInflater mInflater;
    private final MainActivityFragment.Listener mListener;
    private Drawable mPlaceholder;
    private boolean mSingleChoiceMode = false;
    private int mSelectedPosition = -1;
    private Drawable mSelectedBackground;

    public MovieAdapter(Context context, ArrayList<Movie> movies, MainActivityFragment.Listener listener,
                         boolean singleChoiceMode, int selectedPosition,
                         boolean notifyInitialSelectedPosition){
        mMovies = movies;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        mSingleChoiceMode = singleChoiceMode;
        mSelectedPosition = selectedPosition;

        mPlaceholder = ContextCompat.getDrawable(context, R.drawable.sample_0);
        mNoImageDrawable = Utility.getTintedDrawable(context, R.drawable.sample_0, 0);

        if(mSingleChoiceMode){
            mSelectedBackground = ContextCompat.getDrawable(context, R.drawable.selected_background);

            if (notifyInitialSelectedPosition && mSelectedPosition >= 0) {
                listener.onItemSelected(mMovies.get(mSelectedPosition));
            }
        }

        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.grid_item_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mSingleChoiceMode){
            holder.mContainer.setBackground(
                    position == mSelectedPosition ? mSelectedBackground : null);
        }

        String posterPath = mMovies.get(position).getPoster();
        if(posterPath == null){
            holder.mPoster.setImageDrawable(mNoImageDrawable);
            return;
        }

        Picasso.with(holder.mPoster.getContext())
                .load(Utility.getPosterUrl(mMovies.get(position).getPoster()))
                .placeholder(mPlaceholder)
                .error(mNoImageDrawable)
                .into(holder.mPoster);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @Override
    public int getItemCount(){
        return mMovies.size();
    }

    @Override
    public long getItemId(int position) {
        Movie movie = mMovies.get(position);
        if (movie != null)
        {
            return movie.getId();
        }
        else
        {
            return super.getItemId(position);
        }
    }

    public ArrayList<Movie> getItems() {
        return mMovies;
    }

    public int getSelectedPosition(){
        return mSelectedPosition;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mPoster;
        public View mContainer;

        public ViewHolder(View v) {
            super(v);
            mPoster = (ImageView) v.findViewById(R.id.poster);
            mContainer = v.findViewById(R.id.poster_container);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newSelectedPosition = getAdapterPosition();
                    if (mSingleChoiceMode && mSelectedPosition != newSelectedPosition) {
                        if (mSelectedPosition != -1 && mRecyclerView != null) {
                            ViewHolder viewHolder = (ViewHolder) mRecyclerView
                                    .findViewHolderForAdapterPosition(mSelectedPosition);

                            if (viewHolder != null) {
                                viewHolder.mContainer.setBackground(null);
                            }
                        }

                        mSelectedPosition = newSelectedPosition;
                        mContainer.setBackground(mSelectedBackground);

                    }
                    mListener.onItemSelected(mMovies.get(newSelectedPosition));
                }
            });
        }
    }
}
