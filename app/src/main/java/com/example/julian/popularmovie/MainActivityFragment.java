package com.example.julian.popularmovie;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.Vector;


public class MainActivityFragment extends Fragment {

    Movie[] movieArray;
    ImageAdapter imageAdapter;
    ArrayList<Movie> movieArrayList;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null || !savedInstanceState.containsKey("movieKey")){
            movieArrayList = new ArrayList<Movie>();
            updateMovie();
        } else {
            movieArrayList = savedInstanceState.getParcelableArrayList("movieKey");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(movieArrayList != null){
            outState.putParcelableArrayList("movieKey", movieArrayList);
        }
        super.onSaveInstanceState(outState);
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
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("original_title", movieArrayList.get(position).title);
                intent.putExtra("overview", movieArrayList.get(position).description);
                intent.putExtra("poster_path", movieArrayList.get(position).poster);
                intent.putExtra("release_date", movieArrayList.get(position).releaseDate);
                intent.putExtra("vote_average", movieArrayList.get(position).voteAverage);
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onResume() {
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(listener);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    updateMovie();
                }
            };

    private void updateMovie(){


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.most_popular));

        Uri uri;
        String url;

        if(sortOrder == getString(R.string.most_popular)){
            uri = Uri.parse(getString(R.string.movie_Base_Url))
                    .buildUpon()
                    .appendQueryParameter("sort_by", "popularity.desc")
                    .build();
        } else {
            uri = Uri.parse(getString(R.string.movie_Base_Url))
                    .buildUpon()
                    .appendQueryParameter("sort_by", "vote_count.desc")
                    .build();
        }

        url = uri.toString() + "&api_key=" + getString(R.string.api_key);

        if(isNetworkAvailable()){
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(url);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    // TODO: Delete this block of code
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
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieInfoArray.length());

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
                moviePosterPath  = getString(R.string.poster_base) + movie.getString(POSTER_PATH);

                Movie movieObj = new Movie(movieReleaseDate, movieTitle, moviePosterPath, movieDescription, movieAverageVote);
                movieArray[i] = movieObj;

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
