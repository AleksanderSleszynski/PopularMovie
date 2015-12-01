package com.example.julian.popularmovie;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TheMovieDbService {
    @GET("/discover/movie")
    void listMovies(@Query("sort_by") String sort);//, Callback<MoviesResponse> cb);

    @GET("/movie/{movieId}/videos")
    void listVideos(@Path("movieId") long movieId);//, Callback<VideosResponse> cb);

    @GET("/movie/{movieId}/reviews")
    void listReviews(@Path("movieId") long movieId);//, Callback<ReviewsResponse> cb);

}
