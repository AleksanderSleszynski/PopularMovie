package com.example.julian.popularmovie;


import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class MovieCursorAdapter extends RecyclerView.Adapter<MovieCursorAdapter.ViewHolder> {

    private int mSelectedPosition = -1;


    private int selectedPosition;

    @Override
    public MovieCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MovieCursorAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }
}
