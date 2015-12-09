package com.example.julian.popularmovie;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.julian.popularmovie.model.Video;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class Utility {
    public static final String SORT_POPULARITY_DESC = "popularity.desc";
    public static final String SORT_RATING_DESC = "vote_average.desc";

    public static final String POSTER_SIZE_342 = "w342";

    private static final String IMAGES_BASE_URL = "https://image.tmdb.org/t/p/";
    private static TheMovieDbService sInstance;

    private static final int[] TEMP_ARRAY = new int[1];

    public static TheMovieDbService getInstance(){
        if (sInstance == null){
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @SuppressLint("SimpleDateFormat")
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                        @Override
                        public Date deserialize(final JsonElement json, final Type typeOfT,
                                                final JsonDeserializationContext context)
                                throws JsonParseException{
                            try{
                                return df.parse(json.getAsString());
                            }
                            catch (ParseException e){
                                return null;
                            }
                        }
                    })
                    .create();

            RequestInterceptor requestInterceptor = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request){
                    request.addQueryParam("api_key", Config.MOVIE_DB_API_KEY);
                }
            };

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.MOVIE_DB_API_ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .setRequestInterceptor(requestInterceptor)
                    .build();


            sInstance = restAdapter.create(TheMovieDbService.class);
        }

        return sInstance;
    }

    public static String getPosterUrl(String posterPath) {
        return getPosterUrl(posterPath, POSTER_SIZE_342);
    }

    public static String getPosterUrl(String posterPath, String posterSize) {
        return IMAGES_BASE_URL + posterSize + posterPath;
    }

    public static String getTrailerThumbnailUrl(Video video) {
        return "http://img.youtube.com/vi/" + video.getKey() + "/hqdefault.jpg";
    }

    public static int getScreenWidthDp(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return pxToDp(context, size.x);
    }

    public static int pxToDp(Context context, int px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    public static int getThemeAttrColor(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        }
        finally {
            a.recycle();
        }
    }

    public static Drawable getTintedDrawable(Context context, int resId, int tint) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable = drawable.mutate();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, tint);
        return drawable;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityDestroyed(Activity activity)
    {
        return activity == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity.isDestroyed();
    }

    public static void startYouTubeVideo(Context context, String videoId) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + videoId));
            context.startActivity(intent);
        }
    }

    public static void shareYoutubeVideo(Context context, String videoId) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + videoId);
            context.startActivity(shareIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.error_share_youtube, Toast.LENGTH_SHORT).show();
        }
    }

    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
