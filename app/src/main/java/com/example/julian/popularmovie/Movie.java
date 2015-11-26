package com.example.julian.popularmovie;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    String releaseDate;
    String title;
    String poster;
    String description;
    String voteAverage;


    public Movie(String releaseDate, String title, String poster, String description, String voteAverage) {
        this.description = description;
        this.poster      = poster;
        this.releaseDate = releaseDate;
        this.title       = title;
        this.voteAverage = voteAverage;
    }

    protected Movie(Parcel in) {
        releaseDate = in.readString();
        title       = in.readString();
        poster      = in.readString();
        description = in.readString();
        voteAverage = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(releaseDate);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(description);
        dest.writeString(voteAverage);
    }

    public String getPosterUrl(){
        return this.poster;
    }
}
