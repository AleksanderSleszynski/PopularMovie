package com.example.julian.popularmovie.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.julian.popularmovie.data.MovieContract.MovieEntry;


public class MovieDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABEL_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_TITLE +  " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_AVERAGE_VOTE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_FAVOURITE + " BOOLEAN NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + MovieEntry.TABEL_NAME);
        onCreate(db);
    }
}
