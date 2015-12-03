package com.example.julian.popularmovie.model;

import java.util.ArrayList;

public class MovieResponse {
    private int page;
    private ArrayList<Movie> results;

    public ArrayList<Movie> getMovies() {
        return results;
    }
}
