package com.example.julian.popularmovie.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.julian.popularmovie.model.Movie;
import com.example.julian.popularmovie.model.Review;
import com.example.julian.popularmovie.model.Video;
import com.example.julian.popularmovie.data.MovieContract.MovieEntry;
import com.example.julian.popularmovie.data.MovieContract.ReviewEntry;
import com.example.julian.popularmovie.data.MovieContract.VideoEntry;

import java.util.Date;


public class MovieDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_TITLE +  " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DESCRIPTION + " TEXT, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_AVERAGE_VOTE + " DOUBLE NOT NULL" +
                " );";

        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VideoEntry.COLUMNT_MOVIE_ID + " INTEGER NOT NULL, " +
                VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + VideoEntry.COLUMNT_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" +MovieEntry._ID + ") ON DELETE CASCADE);";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewEntry.COLUMN_MOVIE_ID + "INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLMUN_CONTENT + "TEXT NOT NULL, " +
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") ON DELETE CASCADE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_VIDEOS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXIST " + VideoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXIST " + ReviewEntry.TABLE_NAME);
        onCreate(db);
    }

    public static ContentValues toContentValues(Movie movie){
        ContentValues values = new ContentValues();

        values.put(MovieEntry._ID, movie.getId());
        values.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieEntry.COLUMN_DESCRIPTION, movie.getDescription());
        values.put(MovieEntry.COLUMN_RELEASE_DATE,
                movie.getReleaseDate() != null ? movie.getReleaseDate().getTime() : null);
        values.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPoster());
        values.put(MovieEntry.COLUMN_AVERAGE_VOTE, movie.getVoteAverage());

        return values;
    }

    public static ContentValues toContentValues(Video video, long movieId){
        ContentValues values = new ContentValues();

        values.put(VideoEntry.COLUMNT_MOVIE_ID, movieId);
        values.put(VideoEntry.COLUMN_KEY, video.getKey());

        return values;
    }

    public static ContentValues toContentValues(Review review, long movieId){
        ContentValues values = new ContentValues();

        values.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
        values.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
        values.put(ReviewEntry.COLMUN_CONTENT, review.getContent());

        return values;
    }

    public static Movie toMovie(Cursor cursor){
        Movie movie = new Movie();

        movie.setId(cursor.getLong(cursor.getColumnIndex(MovieEntry._ID)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
        movie.setDescription(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DESCRIPTION)));
        if(!cursor.isNull(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE))){
            movie.setReleaseDate(new Date(cursor.getLong(
                    cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE))));
        }
        movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));

        return movie;
    }

    public static Video toVideo(Cursor cursor){
        Video video = new Video();

        video.setSite(Video.SITE_YOUTUBE);
        video.setKey(cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_KEY)));

        return video;
    }

    public static Review toReview(Cursor cursor){
        Review review = new Review();

        review.setAuthor(cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR)));
        review.setContent(cursor.getString(cursor.getColumnIndex(ReviewEntry.COLMUN_CONTENT)));

        return review;
    }
}
