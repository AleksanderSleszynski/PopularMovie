package com.example.julian.popularmovie.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {


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
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Jurassic World");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Twenty-two years after the events of Jurassic Park");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
        movieValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, 6.9);
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");


        return movieValues;
    }



}
