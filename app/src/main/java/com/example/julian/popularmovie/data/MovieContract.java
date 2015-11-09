package com.example.julian.popularmovie.data;

import android.provider.BaseColumns;
import android.text.format.Time;


/**
 * Created by julain on 07.11.15.
 */
public class MovieContract {


    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class MovieEntry implements BaseColumns{

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_AVERAGE_VOTE = "average_vote";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_FAVOURITE = "favourite";
        //public static final String COLUMN_DATE = "date";

    }
}
