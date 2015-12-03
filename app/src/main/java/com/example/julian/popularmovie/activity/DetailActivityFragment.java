package com.example.julian.popularmovie.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.julian.popularmovie.R;
import com.example.julian.popularmovie.Utility;
import com.example.julian.popularmovie.WeakAsyncTask;
import com.example.julian.popularmovie.data.MovieContract;
import com.example.julian.popularmovie.data.MovieContract.MovieEntry;
import com.example.julian.popularmovie.data.MovieDbHelper;
import com.example.julian.popularmovie.data.MovieProvider;
import com.example.julian.popularmovie.model.ApiError;
import com.example.julian.popularmovie.model.Movie;
import com.example.julian.popularmovie.model.Review;
import com.example.julian.popularmovie.model.ReviewResponse;
import com.example.julian.popularmovie.model.Video;
import com.example.julian.popularmovie.model.VideoResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, WeakAsyncTask.WeakAsyncTaskCallbacks {

    public static final String ARG_MOVIE = "ARG_MOVIE";

    private static final String TAG = DetailActivityFragment.class.getSimpleName();
    private static final String STATE_TRAILERS = "STATE_TRAILERS";

    private static final String STATE_REVIEWS = "STATE_REVIEWS";
    private static final String STATE_MOVIE_FAVORITE = "STATE_MOVIE_FAVORITE";
    private static final int TRAILER_THUMBNAIL_SPACING = 8;
    private static final int FAVOURITE_TAG = 0;

    private static final int VIDEOS_TAG = 1;
    private static final int REVIEWS_TAG = 2;

    private Movie mMovie;
    private Boolean mMovieIsFavorite;

    private ImageView mFavoriteImageView;

    private ArrayList<Video> mTrailers = new ArrayList<>(0);
    private LinearLayout mTrailersContainer;
    private View mTrailersSection;
    private boolean mTrailersInitilized = false;

    private ArrayList<Review> mReviews = new ArrayList<>(0);
    private LinearLayout mReviewsContainer;
    private View mReviewsSection;
    private boolean mReviewsInitialized = false;

    private MenuItem mShareMenuItem;
    private Drawable mNoImageDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(ARG_MOVIE);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (mMovie == null) {
            rootView.findViewById(R.id.main_section).setVisibility(View.GONE);
            return rootView;
        }

        TextView tv = (TextView) rootView.findViewById(R.id.title);
        tv.setText(mMovie.getTitle());

        tv = (TextView) rootView.findViewById(R.id.release_date);
        if (mMovie.getReleaseDate() != null) {
            tv.setText(DateFormat.getMediumDateFormat(getActivity())
                    .format(mMovie.getReleaseDate()));
        } else {
            tv.setVisibility(View.GONE);
        }

        tv = (TextView) rootView.findViewById(R.id.rating);
        String rating = mMovie.getVoteAverage() == 10.0 ? "10" : String.valueOf(mMovie.getVoteAverage());
        tv.setText(rating);

        tv = (TextView) rootView.findViewById(R.id.synopsis);
        tv.setText(mMovie.getDescription());

        final ImageView poster = (ImageView) rootView.findViewById(R.id.poster);
        mNoImageDrawable = Utility.getTintedDrawable(getActivity(),
                R.drawable.sample_0,
                0);

        if (mMovie.getPoster() == null) {
            poster.setImageDrawable(mNoImageDrawable);
            poster.setBackgroundColor(getResources().getColor(R.color.no_image_bg_color));
        } else {
            Glide.with(getContext())
                    .load(Utility.getPosterUrl(mMovie.getPoster(), Utility.POSTER_SIZE_342))
                    .error(mNoImageDrawable)
                    .listener(new RequestListener<String, GlideDrawable>()
                    {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource)
                        {
                            poster.setBackgroundColor(
                                    getResources().getColor(R.color.no_image_bg_color));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource)
                        {
                            return false;
                        }
                    })
                    .into(poster);
        }

        mFavoriteImageView = (ImageView) rootView.findViewById(R.id.favorite);
        mFavoriteImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // haven't completed all requests
                if (mMovieIsFavorite == null
                        || !mTrailersInitilized
                        || !mReviewsInitialized)
                {
                    return;
                }

                saveFavoriteStatus(!mMovieIsFavorite);
            }
        });

        mTrailersContainer = (LinearLayout) rootView.findViewById(R.id.trailers_container);
        mTrailersSection = rootView.findViewById(R.id.trailers_section);

        mReviewsContainer = (LinearLayout) rootView.findViewById(R.id.reviews_container);
        mReviewsSection = rootView.findViewById(R.id.reviews_section);

        if (savedInstanceState != null) {
            mTrailers = savedInstanceState.getParcelableArrayList(STATE_TRAILERS);
            showTrailersIfAny();

            mReviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
            showReviewsIfAny();

            mMovieIsFavorite = savedInstanceState.getBoolean(STATE_MOVIE_FAVORITE);
            setFavoriteIcon(mMovieIsFavorite);

        }
        else {

            getLoaderManager().initLoader(FAVOURITE_TAG, null, this);
        }

        return rootView;

    }

    private void saveFavoriteStatus(boolean favorite){
        mMovieIsFavorite = favorite;
        setFavoriteIcon(mMovieIsFavorite);

        new WeakAsyncTask(FAVOURITE_TAG, this).execute();
    }

    private void setFavoriteIcon(boolean favorite) {
        int drawable = favorite
                ? R.drawable.ic_favorite_24dp
                : R.drawable.ic_favorite_outline_24dp;

        mFavoriteImageView.setImageDrawable(
                Utility.getTintedDrawable(getActivity(), drawable,
                        Utility.getThemeAttrColor(getActivity(), R.attr.colorAccent)));

        mFavoriteImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_TRAILERS, mTrailers);
        outState.putParcelableArrayList(STATE_REVIEWS, mReviews);
        outState.putBoolean(STATE_MOVIE_FAVORITE, mMovieIsFavorite != null && mMovieIsFavorite);
        super.onSaveInstanceState(outState);
    }

    private void showTrailersIfAny() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < mTrailers.size(); i++) {
            final Video video = mTrailers.get(i);

            final View thumbnail = inflater.inflate(R.layout.view_trailer_thumbnail,
                    mTrailersContainer, false);
            ImageView iv = (ImageView) thumbnail.findViewById(R.id.image);

            int leftPadding = thumbnail.getPaddingLeft();
            if (i > 0) {
                leftPadding += Utility.dpToPx(getActivity(), TRAILER_THUMBNAIL_SPACING);
            }

            thumbnail.setPadding(leftPadding, thumbnail.getPaddingTop(),
                    thumbnail.getPaddingRight(), thumbnail.getPaddingBottom());

            Glide.with(this)
                    .load(Utility.getTrailerThumbnailUrl(video))
                    .crossFade()
                    .error(mNoImageDrawable)
                    .into(iv);

            mTrailersContainer.addView(thumbnail);

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.startYouTubeVideo(getActivity(), video.getKey());
                }
            });
        }

        if (mTrailers.size() > 0) {
            mTrailersSection.setVisibility(View.VISIBLE);
        }

        mTrailersInitilized = true;
        getActivity().supportInvalidateOptionsMenu();
    }

    private void showReviewsIfAny() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (Review review : mReviews) {
            View view = inflater.inflate(R.layout.view_review, mReviewsContainer, false);
            TextView author = (TextView) view.findViewById(R.id.author);
            TextView content = (TextView) view.findViewById(R.id.content);

            author.setText(review.getAuthor());
            content.setText(review.getContent());

            mReviewsContainer.addView(view);
        }

        if (mReviews.size() > 0) {
            mReviewsSection.setVisibility(View.VISIBLE);
        }

        mReviewsInitialized = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mShareMenuItem.setVisible(!mTrailersInitilized || mTrailersSection == null
                || mTrailersSection.getVisibility() == View.VISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Utility.shareYoutubeVideo(getActivity(), mTrailers.get(0).getKey());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == FAVOURITE_TAG) {
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieUri(mMovie.getId()),
                    new String[]{MovieEntry._ID},
                    null,
                    null,
                    null
            );
        } else if (id == VIDEOS_TAG) {
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieVideosUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );
        } else if (id == REVIEWS_TAG) {
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieReviewsUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );
        }

        return null;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == FAVOURITE_TAG) {

            mMovieIsFavorite = data.moveToFirst();
            setFavoriteIcon(mMovieIsFavorite);

            Utility.getInstance().listVideos(mMovie.getId(), new VideosResponseCallback(this));
            Utility.getInstance().listReviews(mMovie.getId(), new ReviewsResponseCallback(this));
        } else if (id == VIDEOS_TAG) {
          mTrailers = new ArrayList<>(data.getCount());
            while (data.moveToNext()) {
                mTrailers.add(MovieDbHelper.toVideo(data));
            }

            showTrailersIfAny();
        } else if (id == REVIEWS_TAG) {
            mReviews = new ArrayList<>(data.getCount());
            while (data.moveToNext())
            {
                mReviews.add(MovieDbHelper.toReview(data));
            }

            showReviewsIfAny();
        }

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void processApiError(RetrofitError error) {
        String errorString;
        if (Utility.isConnectedToInternet(getActivity())) {
            try {
                ApiError apiError = (ApiError) error.getBodyAs(ApiError.class);
                errorString = apiError.getStatusMessage();
            } catch (Throwable t) {
                errorString = error.getMessage();
            }
        } else {
            errorString = getString(R.string.error_no_internet);
        }

        if (!mMovieIsFavorite) {
            Toast.makeText(getActivity().getApplicationContext(), errorString, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void insertReviews(ContentResolver resolver) {
        if (mReviews.size() > 0) {
            ContentValues[] values = new ContentValues[mReviews.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = MovieDbHelper.toContentValues(mReviews.get(i), mMovie.getId());
            }

            resolver.bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, values);
        }
    }

    private void insertVideos(ContentResolver resolver) {
        if (mTrailers.size() > 0) {
            ContentValues[] values = new ContentValues[mTrailers.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = MovieDbHelper.toContentValues(mTrailers.get(i), mMovie.getId());
            }

            resolver.bulkInsert(MovieContract.VideoEntry.CONTENT_URI, values);
        }
    }

    @Override
    public void onAsyncTaskDoInBackground(int id) {

        Activity activity = getActivity();
        if (Utility.isActivityDestroyed(activity)) {
            return;
        }

        ContentResolver resolver = activity.getContentResolver();
        if (id == FAVOURITE_TAG) {
            if (mMovieIsFavorite) {
                resolver.insert(MovieEntry.CONTENT_URI, MovieDbHelper.toContentValues(mMovie));
                insertVideos(resolver);
                insertReviews(resolver);
            } else {
                resolver.delete(MovieEntry.buildMovieUri(mMovie.getId()), null, null);
            }
        } else if (id == VIDEOS_TAG) {
            resolver.delete(MovieContract.VideoEntry.CONTENT_URI,
                    MovieProvider.VIDEOS_BY_MOVIE_SELECTION,
                    new String[]{Long.toString(mMovie.getId())});

            insertVideos(resolver);
        } else if (id == REVIEWS_TAG) {
           resolver.delete(MovieContract.ReviewEntry.CONTENT_URI,
                    MovieProvider.REVIEWS_BY_MOVIE_SELECTION,
                    new String[]{Long.toString(mMovie.getId())});

            insertReviews(resolver);
        }
    }

    private static class VideosResponseCallback implements Callback<VideoResponse> {
        private WeakReference<DetailActivityFragment> mWeakFragment;

        public VideosResponseCallback(DetailActivityFragment fragment) {
            mWeakFragment = new WeakReference<>(fragment);
        }

        private DetailActivityFragment getWeakFragment() {
            DetailActivityFragment fragment = mWeakFragment.get();
            if (fragment != null && !Utility.isActivityDestroyed(fragment.getActivity())) {
                return fragment;
            }

            return null;
        }

        @Override
        public void success(VideoResponse videosResponse, Response response) {
            DetailActivityFragment fragment = getWeakFragment();
            if (fragment != null) {

                fragment.mTrailers = new ArrayList<>(videosResponse.getVideos().size());
                for (Video video : videosResponse.getVideos()) {
                    if (video.isValid()) {
                        fragment.mTrailers.add(video);
                    }
                }

                fragment.showTrailersIfAny();

                if (fragment.mMovieIsFavorite) {
                    new WeakAsyncTask(VIDEOS_TAG, fragment).execute();
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            DetailActivityFragment fragment = getWeakFragment();
            if (fragment != null) {
                fragment.mTrailersInitilized = true;
                fragment.processApiError(error);

                if (fragment.mMovieIsFavorite) {
                    // load from db
                    fragment.getLoaderManager().restartLoader(VIDEOS_TAG, null, fragment);
                }
            }
        }
    }

    private static class ReviewsResponseCallback implements Callback<ReviewResponse> {
        private WeakReference<DetailActivityFragment> mWeakFragment;

        public ReviewsResponseCallback(DetailActivityFragment fragment) {
            mWeakFragment = new WeakReference<>(fragment);
        }

        private DetailActivityFragment getWeakFragment() {
            DetailActivityFragment fragment = mWeakFragment.get();
            if (fragment != null && !Utility.isActivityDestroyed(fragment.getActivity())) {
                return fragment;
            }

            return null;
        }

        @Override
        public void success(ReviewResponse reviewsResponse, Response response) {
            DetailActivityFragment fragment = getWeakFragment();
            if (fragment != null) {
                fragment.mReviews = new ArrayList<>(reviewsResponse.getReviews().size());
                for (Review review : reviewsResponse.getReviews()) {
                    if (review.isValid()) {
                        fragment.mReviews.add(review);
                    }
                }

                fragment.showReviewsIfAny();

                if (fragment.mMovieIsFavorite) {
                    new WeakAsyncTask(REVIEWS_TAG, fragment).execute();
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            DetailActivityFragment fragment = getWeakFragment();
            if (fragment != null) {

                fragment.mReviewsInitialized = true;
                fragment.processApiError(error);

                if (fragment.mMovieIsFavorite) {
                    // load from db
                    fragment.getLoaderManager().restartLoader(REVIEWS_TAG, null, fragment);
                }
            }
        }
    }
}
