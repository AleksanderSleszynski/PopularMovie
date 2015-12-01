package com.example.julian.popularmovie;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.example.julian.popularmovie.model.Movie;

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

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_TWO_PANE_MODE = "ARG_TWO_PANE_MODE";

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private static final String STATE_SORT_ORDER = "STATE_SORT_ORDER";
    private static final String STATE_MOVIES = "STATE_MOVIES";
    private static final String STATE_SELECTED_POSITION = "STATE_SELECTED_POSITION";

    private static final String PREFERENCE_KEY = "com.example.julian.popularmovies.MOVIES_LIST_PREFERENCES";

    private static final String SORT_FAVORITES = "SORT_FAVORITES";

    private static final int FAVORITES_LOADER = 1;
    private static final int FAVORITES_LOADER_RESTORED = 2;

    private String mSortOrder = Utility.SORT_POPULARITY_DESC;
    private String mNewSortOrder = mSortOrder;

    @Bind(R.id.empty) TextView mNoFavoritesText;
    @Bind(R.id.error) TextView mErrorText;
    @Bind(R.id.retry) Button mRetryButton;
    @Bind(R.id.progress) ContentLoadingProgressBar mProgressBar;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    private boolean mInProgress;
    private boolean mTwoPaneMode;

    private int mSelectedPosition = 0;
    private MovieCursorAdapter movieCursorAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if(args != null){
            mTwoPaneMode = args.getBoolean(ARG_TWO_PANE_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        mNewSortOrder = mSortOrder = getActivity()
                .getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                .getString(STATE_SORT_ORDER, mSortOrder);

        ArrayList<Movie> movies = null;
        boolean restoredState = false;
        if(savedInstanceState !=  null){
            movies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            restoredState = true;
        }

        if(movies != null) {
            mRecyclerView.setAdapter(new MovieAdapter(getActivity(),
                    movies,
                    (Listener) getActivity(),
                    mTwoPaneMode,
                    mSelectedPosition,
                    false));
        } else {
            final boolean finalRestoredState = restoredState;
            mProgressBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    refresh(finalRestoredState);
                    mProgressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mRecyclerView.getAdapter() instanceof MovieAdapter)
        {
            MovieAdapter adapter = (MovieAdapter) mRecyclerView.getAdapter();
            outState.putParcelableArrayList(STATE_MOVIES, adapter.getItems());
            outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
        }
        else if (mRecyclerView.getAdapter() instanceof MovieCursorAdapter)
        {
            // don't save items (the loader will reload data)
            MovieCursorAdapter adapter = (MovieCursorAdapter) mRecyclerView.getAdapter();
            outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
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
    public void onResume() {
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(listener);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                .edit()
                .putString(STATE_SORT_ORDER, mSortOrder)
                .apply();
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface Listener {
        void onItemSelected(Movie movie);
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
