package com.example.julian.popularmovie.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted form movie table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }

    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

        long testMovie = 102899;

        type = mContext.getContentResolver().getType(
                MovieContract.MovieEntry.buildMovieUri(testMovie)
        );
        assertEquals("Error: the MovieEntry CONTENT_URI with movie should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);
    }

    public void testBasicWeatherQuery() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();

        db.close();

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, testValues);
    }

    public void testUpdateLocation() {
        ContentValues values = TestUtilities.createMovieValues();

        Uri movieUri = mContext.getContentResolver()
                .insert(MovieContract.MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieContract.MovieEntry._ID, movieRowId);
        updatedValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Jurassic World");
        updatedValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, "Twenty-two years after the events of Jurassic Park");
        updatedValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
        updatedValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, 6.9);
        updatedValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
        updatedValues.put(MovieContract.MovieEntry.COLUMN_FAVOURITE, true);

        Cursor movieCursor = mContext.getContentResolver()
                .query(
                        MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver()
                .update(
                        MovieContract.MovieEntry.CONTENT_URI,
                        updatedValues,
                        MovieContract.MovieEntry._ID + "= ?",
                        new String[]{Long.toString(movieRowId)}
                );
        assertEquals(count, 1);

        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry._ID,
                null,
                null
        );

        TestUtilities.validateCursor("testUpdatedLocation. Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createMovieValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowid = ContentUris.parseId(movieUri);
        assertTrue(movieRowid != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);

        ContentValues movieValues = TestUtilities.createMovieValues();
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, tco);

        Uri movieInsertUri = mContext.getContentResolver()
                .insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
        assertTrue(movieInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert",
                movieCursor, movieValues);

        movieValues.putAll(testValues);

        movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUri(TestUtilities.TEST_MOVIE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined",
                movieCursor, movieValues);
    }

    public void testDeletedRecords(){
        testInsertReadProvider();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver()
                .registerContentObserver(
                        MovieContract.MovieEntry.CONTENT_URI,
                        true,
                        movieObserver
                );

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver()
                .unregisterContentObserver(movieObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 7;
    static ContentValues[] createBulkInsertMovieValues(long movieRowId){
        ContentValues[] returnContetnValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for(int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++){
           ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry._ID, movieRowId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Jurassic World");
            movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, "Twenty-two years after the events of Jurassic Park");
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
            movieValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, 6.9);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVOURITE, true);
            returnContetnValues[i] = movieValues;
        }
        return returnContetnValues;
    }


    public void testBulkInsert() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, testValues);
        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBulkInsert: Error validating MovieEntry",
                cursor, testValues);

        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues(movieRowId);

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for(int i = 0; i<BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()){
            TestUtilities.validateCurrentRecord("testBulkInsert. Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
