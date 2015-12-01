package com.example.julian.popularmovie;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.julian.popularmovie.data.MovieDbHelper;
import com.example.julian.popularmovie.model.Movie;
import com.squareup.picasso.Picasso;

public class MovieCursorAdapter extends RecyclerView.Adapter<MovieCursorAdapter.ViewHolder> {

    private RecyclerView mRecyclerView;
    private final Drawable mNoImageDrawable;
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private final MainActivityFragment.Listener mListener;
    private Drawable mPlaceholder;
    private boolean mSingleChoiceMode = false;
    private int mSelectedPosition = -1;
    private Drawable mSelectedBackground;

    public MovieCursorAdapter(Context context, Cursor cursor,
                               MainActivityFragment.Listener listener,
                               boolean singleChoiceMode, int selectedPosition,
                               boolean notifyInitialSelectedPosition){
        mCursor = cursor;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        mSingleChoiceMode = singleChoiceMode;
        mSelectedPosition = selectedPosition;

        mPlaceholder = ContextCompat.getDrawable(context, R.drawable.sample_0);
        mNoImageDrawable = Utility.getTintedDrawable(context, R.drawable.sample_0, 0);

        if(mSingleChoiceMode){
            mSelectedBackground = ContextCompat.getDrawable(context, R.drawable.selected_background);
            if(mCursor != null && notifyInitialSelectedPosition){
                notifySelectedPosition(selectedPosition);
                mCursor.moveToFirst();
            }
        }
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.grid_item_movie, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(MovieCursorAdapter.ViewHolder holder, int position){
        if(!mCursor.moveToPosition(position)){return;}

        if(mSingleChoiceMode){
            holder.mContainer.setBackground(
                    position == mSelectedPosition ? mSelectedBackground : null);
        }

        Movie movie= MovieDbHelper.toMovie(mCursor);

        String posterPath = movie.getPoster();
        if(posterPath == null){
            holder.mPoster.setImageDrawable(mNoImageDrawable);
        }

        Picasso.with(holder.mPoster.getContext())
                .load(Utility.getPosterUrl(posterPath))
                .placeholder(mPlaceholder)
                .error(mNoImageDrawable)
                .into(holder.mPoster);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView){
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    private void notifySelectedPosition(int selectedPosition){
        if (mCursor == null || !mCursor.moveToPosition(selectedPosition)){
            mListener.onItemSelected(null);
            return;
        }
        mListener.onItemSelected(MovieDbHelper.toMovie(mCursor));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null){
            return mCursor.getCount();
        }
        return 0;
    }

    public long getItemId(Cursor cursor, int position){
        if (cursor != null && cursor.moveToPosition(position)){
            return cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        }
        return -1;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public Cursor swapCursor(Cursor cursor){
        if (mCursor == cursor) return null;

        boolean needsNotification = false;
        if(mSingleChoiceMode){
            int newSelectedPosition = mSelectedPosition;
            if(cursor != null){
                if(mSelectedPosition >= cursor.getCount()){
                    newSelectedPosition = cursor.getCount() - 1;
                } else if( mSelectedPosition == -1){
                    newSelectedPosition = 0;
                }
            } else {
                newSelectedPosition = -1;
            }

            long oldSelectedId = getItemId(mSelectedPosition);
            long newSelectedId = getItemId(cursor, newSelectedPosition);

            if(oldSelectedId != newSelectedId){
                needsNotification = true;
            }

            mSelectedPosition = newSelectedPosition;
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if(needsNotification){
            notifySelectedPosition(mSelectedPosition);
        }

        notifyDataSetChanged();

        return oldCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
                            ViewHolder vh = (ViewHolder) mRecyclerView
                                    .findViewHolderForAdapterPosition(mSelectedPosition);
                            if (vh != null) {
                                vh.mContainer.setBackground(null);
                            }
                        }
                        mSelectedPosition = newSelectedPosition;
                        mContainer.setBackground(mSelectedBackground);

                        notifySelectedPosition(newSelectedPosition);
                    }
                }
            });
        }
    }
}
