package com.example.julian.popularmovie;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    @Bind(R.id.detail_title) TextView textViewMovieTitle;
    @Bind(R.id.detail_poster) ImageView posterImageView;
    @Bind(R.id.detail_release_date)  TextView textViewReleaseDate;
    @Bind(R.id.detail_vote_average) TextView textViewVoteAverage;
    @Bind(R.id.detail_description) TextView textViewDescription;

    public static final String ARG_MOVIE = "ARG_MOVIE";
    private static final String TAG = DetailActivityFragment.class.getSimpleName();

    private static final String STATE_TRAILERS = "STATE_TRAILERS";
    private static final String STATE_REVIEWS = "STATE_REVIEWS";
    private static final String STATE_MOVIE_FAVORITE = "STATE_MOVIE_FAVORITE";
    private static final int TRAILER_THUMBNAIL_SPACING = 8;

    private static final int FAVOURITE_TAG = 0;
    private static final int VIDEOS_TAG = 1;
    private static final int REVIEWS_TAG = 2;




    public DetailActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        Intent intent = getActivity().getIntent();

        String movieTitle = intent.getStringExtra("original_title");
        textViewMovieTitle.setText(movieTitle);

        String posterUrl = intent.getStringExtra("poster_path");
        Picasso.with(getContext())
                .load(posterUrl)
                .error(R.drawable.sample_0)
                .into(posterImageView);


        String releaseDate = intent.getStringExtra("release_date");

        if(releaseDate.length() >= 4) textViewReleaseDate.setText(releaseDate.substring(0,4));

        String voteAverage = intent.getStringExtra("vote_average");
        textViewVoteAverage.setText(voteAverage + "/10");

        String description = intent.getStringExtra("overview");
        textViewDescription.setText(description);

        return rootView;


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
