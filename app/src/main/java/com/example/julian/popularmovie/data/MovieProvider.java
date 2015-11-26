package com.example.julian.popularmovie.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    private static final int MOVIES = 100;
    private static final int MOVIE = 101;
    private static final int MOVIE_VIDEOS = 102;
    private static final int MOVIE_REVIEWS = 103;
    private static final int VIDEOS = 200;
    private static final int REVIEWS = 300;

    public static final String MOVIE_BY_ID_SELECTION = MovieContract.MovieEntry.TABLE_NAME
            + "." + MovieContract.MovieEntry._ID + " = ? ";

    public static final String VIDEOS_BY_MOVIE_SELECTION = MovieContract.VideoEntry.TABLE_NAME
            + "." + MovieContract.VideoEntry.COLUMNT_MOVIE_ID + " = ? ";

    public static final String REVIEWS_BY_MOVIE_SELECTION = MovieContract.ReviewEntry.TABLE_NAME
            + "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

//    private static final SQLiteQueryBuilder sMovieSettingQueryBuilder;
//
//    static {
//        sMovieSettingQueryBuilder = new SQLiteQueryBuilder();
//        sMovieSettingQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
//    }

    private Cursor getMovie(Uri uri, String[] projection, String sortOrder) {
        return null;
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_VIDEOS, MOVIE_VIDEOS);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_REVIEWS, MOVIE_REVIEWS);
        uriMatcher.addURI(authority, MovieContract.PATH_VIDEOS, VIDEOS);
        uriMatcher.addURI(authority, MovieContract.PATH_REVIEWS, REVIEWS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MOVIE_BY_ID_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))},
                        null,
                        null,
                        null
                );
                break;
            case MOVIE_VIDEOS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        VIDEOS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_REVIEWS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        REVIEWS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_VIDEOS:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case VIDEOS:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case MOVIE_REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";

        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case MOVIE:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        MOVIE_BY_ID_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))});
                break;

            case MOVIE_VIDEOS:
                rowsDeleted = db.delete(
                        MovieContract.VideoEntry.TABLE_NAME,
                        VIDEOS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))});
                break;

            case MOVIE_REVIEWS:
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        REVIEWS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MovieContract.parseId(uri))});
                break;
            case VIDEOS:
                rowsDeleted = db.delete(MovieContract.VideoEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case REVIEWS:
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES: {
                rowsUpdated = db.update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;

            case VIDEOS:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;

            case REVIEWS:
                tableName = MovieContract.ReviewEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);

        }

        int returnCount = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;


    }
}
