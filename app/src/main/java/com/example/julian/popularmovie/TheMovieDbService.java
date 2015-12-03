package com.example.julian.popularmovie;

import com.example.julian.popularmovie.model.MovieResponse;
import com.example.julian.popularmovie.model.ReviewResponse;
import com.example.julian.popularmovie.model.VideoResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TheMovieDbService {
    @GET("/discover/movie")
    void listMovies(@Query("sort_by") String sort, Callback<MovieResponse> cb);

    @GET("/movie/{movieId}/videos")
    void listVideos(@Path("movieId") long movieId,  Callback<VideoResponse> cb);

    @GET("/movie/{movieId}/reviews")
    void listReviews(@Path("movieId") long movieId, Callback<ReviewResponse> cb);

}
