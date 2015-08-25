package com.example.julian.popularmovie;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivityFragment extends Fragment {

    Movie[] movieArray;
    ImageAdapter imageAdapter;
    ArrayList<Movie> movieArrayList;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);

        imageAdapter  = new ImageAdapter(getActivity(), movieArrayList);
        gridView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("PENIS", "onStart movieTask");
        updateMovie();
    }

    private void updateMovie(){
        FetchMovieTask movieTask = new FetchMovieTask();
        Log.d("PENIS", "Odpalam movieTask");
        movieTask.execute();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        private Movie[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            final String DESCRIPTION    = "overview";
            final String ORIGINAL_TITLE = "original_title";
            final String POSTER_PATH    = "poster_path";
            final String RELEASE_DATE   = "release_date";
            final String RESULTS        = "results";
            final String VOTE_AVG       = "vote_average";

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

                movieTitle       = movie.getString(ORIGINAL_TITLE);
                movieDescription = movie.getString(DESCRIPTION);
                movieReleaseDate = movie.getString(RELEASE_DATE);
                movieAverageVote = movie.getString(VOTE_AVG);
                moviePosterPath  = "http://image.tmdb.org/t/p/w185/" + movie.getString(POSTER_PATH);

                Movie movieObj = new Movie(movieReleaseDate, movieTitle, moviePosterPath, movieDescription, movieAverageVote);
                movieArray[i] = movieObj;
            }

            return movieArray;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            //Declare this outside the try/catch
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Will contain the raw JSON response as a string
            String movieJsonStr = null;

            String movieUrl = params[0];

//            String apiKey = "c1fa741e11cef0f1559b00acc6e86fff";

//            // This place i need to choose which param will be chosen to request
//            String popularity = "popularity.desc";
//            String voteAverage = "vote_average.desc";
//
//            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
//            final String SORT_PARAM = "sort_by";
//            final String API_KEY_PARAM = "api_key";

//            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
//                    .appendQueryParameter(SORT_PARAM, popularity)
//                    .appendQueryParameter(API_KEY_PARAM, apiKey)
//                    .build();
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
                    return  null;
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
                imageAdapter.clear();
                for (int i = 0; i < result.length; i++) {
                    imageAdapter.add(result[i]);
                }
            }
        }
    }

}
