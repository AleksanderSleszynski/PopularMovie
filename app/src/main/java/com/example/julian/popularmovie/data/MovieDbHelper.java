package com.example.julian.popularmovie.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.julian.popularmovie.data.MovieContract.MovieEntry;
import com.example.julian.popularmovie.data.MovieContract.VideoEntry;
import com.example.julian.popularmovie.data.MovieContract.ReviewEntry;


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
}
