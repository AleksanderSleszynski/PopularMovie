package com.example.julian.popularmovie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();

        String movieTitle = intent.getStringExtra("original_title");
        TextView textViewMovieTitle = (TextView) rootView.findViewById(R.id.detail_title);
        textViewMovieTitle.setText(movieTitle);

        String posterUrl = intent.getStringExtra("poster_path");
        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.detail_poster);
        Picasso.with(getContext())
                .load(posterUrl)
                .into(posterImageView);


        String releaseDate = intent.getStringExtra("release_date");
        TextView textViewReleaseDate = (TextView) rootView.findViewById(R.id.detail_release_date);

        if(releaseDate.length() >= 4) textViewReleaseDate.setText(releaseDate.substring(0,4));

        String voteAverage = intent.getStringExtra("vote_average");
        TextView textViewVoteAverage = (TextView) rootView.findViewById(R.id.detail_vote_average);
        textViewVoteAverage.setText(voteAverage + "/10");

        String description = intent.getStringExtra("overview");
        TextView textViewDescription = (TextView) rootView.findViewById(R.id.detail_description);
        textViewDescription.setText(description);

        return rootView;


    }
}
