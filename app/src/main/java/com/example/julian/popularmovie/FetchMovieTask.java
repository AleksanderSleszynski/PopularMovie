package com.example.julian.popularmovie;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.julian.popularmovie.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private final Context mContext;
    private ImageAdapter mImageAdapter;
    Movie[] movieArray;

    public FetchMovieTask(Context context, ImageAdapter imageAdapter) {
        mContext = context;
        mImageAdapter = imageAdapter;
    }

    private boolean DEBUG = true;

    private Movie[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        final String DESCRIPTION = "overview";
        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String RELEASE_DATE = "release_date";
        final String RESULTS = "results";
        final String VOTE_AVG = "vote_average";

        JSONObject movieJsonObject = new JSONObject(movieJsonStr);
        JSONArray movieInfoArray = movieJsonObject.getJSONArray(RESULTS);

        movieArray = new Movie[movieInfoArray.length()];

        String movieTitle;
        String movieDescription;
        String movieReleaseDate;
        String movieAverageVote;
        String moviePosterPath;

        for (int i = 0; i < movieInfoArray.length(); i++) {

            JSONObject movie = movieInfoArray.getJSONObject(i);

            movieTitle = movie.getString(ORIGINAL_TITLE);
            movieDescription = movie.getString(DESCRIPTION);
            movieReleaseDate = movie.getString(RELEASE_DATE);
            movieAverageVote = movie.getString(VOTE_AVG);
            moviePosterPath = mContext.getString(R.string.poster_base) + movie.getString(POSTER_PATH);
            // TODO: Here we should add trailers etc...



            Movie movieObj = new Movie(movieReleaseDate, movieTitle, moviePosterPath, movieDescription, movieAverageVote);
            movieArray[i] = movieObj;

            ContentValues movieValues  =new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieTitle);
            movieValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, movieDescription);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
            movieValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, movieAverageVote);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
        }

        return movieArray;
    }

    @Override
    protected Movie[] doInBackground(String... params) {

        //Declare this outside the try/catch
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //Will contain the raw JSON response as a string
        String movieJsonStr = null;

        String movieUrl = params[0];

        try {
            URL url = new URL(movieUrl);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieDataFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Movie[] result) {
        if (result != null) {
            mImageAdapter.clear();
            for (int i = 0; i < result.length; i++) {
                mImageAdapter.add(result[i]);
            }
        }
    }
}

