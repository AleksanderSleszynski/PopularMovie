package com.example.julian.popularmovie.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Movie implements Parcelable {

    private long id;
    private String originalTitle;
    private String overview;
    private Date releaseDate;
    private String posterPath;
    private float voteAverage;

    public Movie(){}

    protected Movie(Parcel in) {
        id          = in.readLong();
        originalTitle = in.readString();
        overview = in.readString();
        long time   = in.readLong();
        releaseDate = time != 0 ? new Date(time) : null;
        posterPath = in.readString();
        voteAverage = in.readFloat();
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
        dest.writeLong(id);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : 0);
        dest.writeString(posterPath);
        dest.writeFloat(voteAverage);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setPosterPath(String poster) {
        this.posterPath = poster;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public long getId() {
        return id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

}
