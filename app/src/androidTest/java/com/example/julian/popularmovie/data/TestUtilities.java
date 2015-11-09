package com.example.julian.popularmovie.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.julian.popularmovie.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "'not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" + expectedValue + "'. " +
                    error, expectedValue, valueCursor.getColumnName(idx));

        }

    }

    // Use this to create some default movie values for my database.

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Jurassic World");
        movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, "Twenty-two years after the events of Jurassic Park");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
        movieValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, 6.9);
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVOURITE, true);


        return movieValues;
    }

    static long insertMovieValues(Context context) {
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Movie Values", locationRowId != -1);

        return locationRowId;

    }

    static class TestContentObserver extends ContentObserver {

        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver(){
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        public TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier version of Android, this onChange method is called

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onChange(selfChange, uri);
        }

        public void waitForNotificationOrFail() {
            new PollingCheck(5000){
                @Override
                protected boolean check(){
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver(){
        return TestContentObserver.getTestContentObserver();
    }

}
