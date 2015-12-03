package com.example.julian.popularmovie.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julian.popularmovie.MovieAdapter;
import com.example.julian.popularmovie.MovieCursorAdapter;
import com.example.julian.popularmovie.R;
import com.example.julian.popularmovie.Utility;
import com.example.julian.popularmovie.data.MovieContract;
import com.example.julian.popularmovie.model.ApiError;
import com.example.julian.popularmovie.model.Movie;
import com.example.julian.popularmovie.model.MovieResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


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

    @Bind(R.id.empty)
    TextView mNoFavoritesText;
    @Bind(R.id.error)
    TextView mErrorText;
    @Bind(R.id.retry)
    Button mRetryButton;
    @Bind(R.id.progress)
    ContentLoadingProgressBar mProgressBar;
    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private boolean mInProgress;
    private boolean mTwoPaneMode;

    private int mSelectedPosition = 0;
    private MovieCursorAdapter mMovieCursorAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
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
        if (savedInstanceState != null) {
            movies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            restoredState = true;
        }

        if (movies != null) {
            mRecyclerView.setAdapter(new MovieAdapter(getActivity(),
                    movies,
                    (Listener) getActivity(),
                    mTwoPaneMode,
                    mSelectedPosition,
                    false));
        } else {
            final boolean finalRestoredState = restoredState;
            mProgressBar.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
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
        if (mRecyclerView.getAdapter() != null) {
            if (mRecyclerView.getAdapter() instanceof MovieAdapter) {
                MovieAdapter adapter = (MovieAdapter) mRecyclerView.getAdapter();
                outState.putParcelableArrayList(STATE_MOVIES, adapter.getItems());
                outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
            } else if (mRecyclerView.getAdapter() instanceof MovieCursorAdapter) {
                // don't save items (the loader will reload data)
                MovieCursorAdapter adapter = (MovieCursorAdapter) mRecyclerView.getAdapter();
                outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
            }
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort) {
            int checkedItem = 0;
            if (mSortOrder.equals(Utility.SORT_RATING_DESC)) {
                checkedItem = 1;
            } else if (mSortOrder.equals(SORT_FAVORITES)) {
                checkedItem = 2;
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(R.array.movie_sort_options,
                            checkedItem,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mNewSortOrder = Utility.SORT_POPULARITY_DESC;

                                    if (which == 1) {
                                        mNewSortOrder = Utility.SORT_RATING_DESC;
                                    } else if (which == 2) {
                                        mNewSortOrder = SORT_FAVORITES;
                                    }

                                    if (!mNewSortOrder.equals(mSortOrder)) {
                                        mSelectedPosition = 0;
                                        mMovieCursorAdapter = null;
                                        refresh(mNewSortOrder, false);
                                    }

                                    dialog.dismiss();
                                }
                            })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!mNewSortOrder.equals(SORT_FAVORITES)) {
            return;
        }

        setUiInProgress(false);
        mSortOrder = mNewSortOrder;

        if (mMovieCursorAdapter == null) {
            mMovieCursorAdapter = new MovieCursorAdapter(getActivity(),
                    data, (Listener) getActivity(), mTwoPaneMode, mSelectedPosition,
                    loader.getId() == FAVORITES_LOADER);
        } else {
            mMovieCursorAdapter.swapCursor(data);
        }

        if (mRecyclerView.getAdapter() != mMovieCursorAdapter) {
            mRecyclerView.setAdapter(mMovieCursorAdapter);
        }

        mNoFavoritesText.setVisibility(data == null || data.getCount() == 0
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }

    public interface Listener {
        void onItemSelected(Movie movie);
    }

    private void refresh() {
        refresh(false);
    }

    private void refresh(boolean restoredState) {
        refresh(mSortOrder, restoredState);
    }

    private void refresh(String sortOrder, boolean restoredState) {
        if (mInProgress) {
            return;
        }

        setUiInProgress(true);

        if (sortOrder.equals(SORT_FAVORITES)) {
            getLoaderManager().restartLoader(
                    restoredState ? FAVORITES_LOADER_RESTORED : FAVORITES_LOADER, null, this);
        } else {
            Utility.getInstance().listMovies(sortOrder, new MovieResponseCallback(this));
        }
    }

    private void setUiInProgress(boolean inProgress) {
        mInProgress = inProgress;

        if (inProgress) {
            mErrorText.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
            mProgressBar.show();
        } else {
            mProgressBar.hide();
        }
    }

    private static class MovieResponseCallback implements Callback<MovieResponse> {
        private final WeakReference<MainActivityFragment> mWeakFragment;

        public MovieResponseCallback(MainActivityFragment fragment) {
            mWeakFragment = new WeakReference<>(fragment);
        }

        @Override
        public void success(MovieResponse movie, Response response) {
            MainActivityFragment fragment = mWeakFragment.get();
            if (fragment == null || Utility.isActivityDestroyed(fragment.getActivity())) {
                return;
            }

            fragment.setUiInProgress(false);
            fragment.mSortOrder = fragment.mNewSortOrder;
            fragment.mNoFavoritesText.setVisibility(View.GONE);

            MovieAdapter adapter = new MovieAdapter(fragment.getActivity(),
                    movie.getMovies(), (Listener) fragment.getActivity(),
                    fragment.mTwoPaneMode, fragment.mSelectedPosition, true);
            fragment.mRecyclerView.setAdapter(adapter);
        }

        @Override
        public void failure(RetrofitError error) {
            MainActivityFragment fragment = mWeakFragment.get();
            if (fragment == null || Utility.isActivityDestroyed(fragment.getActivity())) {
                return;
            }

            Activity activity = fragment.getActivity();

            String errorString;
            if (Utility.isConnectedToInternet(activity)) {
                try {
                    ApiError apiError = (ApiError) error.getBodyAs(ApiError.class);
                    errorString = apiError.getStatusMessage();
                } catch (Throwable t) {
                    errorString = error.getMessage();
                }

            } else {
                errorString = fragment.getString(R.string.error_no_internet);
            }

            if (fragment.mRecyclerView.getAdapter() != null) {
                Toast.makeText(activity.getApplicationContext(), errorString, Toast.LENGTH_SHORT)
                        .show();
            } else {
                // no content yet
                fragment.mErrorText.setText(errorString);
                fragment.mErrorText.setVisibility(View.VISIBLE);
                fragment.mRetryButton.setVisibility(View.VISIBLE);
            }

            fragment.setUiInProgress(false);
        }
    }

}
