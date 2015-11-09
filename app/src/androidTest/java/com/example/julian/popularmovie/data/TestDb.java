package com.example.julian.popularmovie.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase(){
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.
        This makes sure we always have a clean test.
    */
    public void setUp(){
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable{
        // build a HashSet of all of the table names we wish to look for

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want ??
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that database has not been created correctly",
                c.moveToFirst());

        // verify that table have been created
        do{
            tableNameHashSet.remove(c.getString(0));
        } while(c.moveToNext() );

        assertTrue("Error: Your database was created without movie entry",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_DESCRIPTION);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_FAVOURITE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // If this fails, it means that your database doesn't contain all of the required
        // movie entry column
        assertTrue("Error: The database doesn't contain all of the required movie columns",
                movieColumnHashSet.isEmpty());
        db.close();

    }

    public long testMovieTable(){
        //Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what i want to insert
        ContentValues testValues = TestUtilities.createMovieValues();

        // Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null , testValues);

        //Verify we got a row back
        assertTrue(locationRowId != -1);

        //Query the database and receive a Cursor back
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME, // Table to query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values fot the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                cursor, testValues);

        assertFalse("Error: More than one record returned for movie query",
                cursor.moveToNext());
        cursor.close();
        db.close();

        return locationRowId;
    }

}
