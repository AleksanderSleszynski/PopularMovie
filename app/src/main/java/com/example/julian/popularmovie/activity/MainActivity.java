package com.example.julian.popularmovie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.example.julian.popularmovie.R;
import com.example.julian.popularmovie.Utility;
import com.example.julian.popularmovie.model.Movie;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Listener {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.movie_detail_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null){
                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                Bundle args = new Bundle();
                args.putBoolean(MainActivityFragment.ARG_TWO_PANE_MODE, true);
                mainActivityFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, mainActivityFragment, null)
                        .replace(R.id.movie_detail_container,
                                new DetailActivityFragment(), null)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(final Movie movie){
        if(mTwoPane){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Utility.isActivityDestroyed(MainActivity.this)) {
                        return;
                    }
                    if(movie != null){
                        Bundle args = new Bundle();
                        args.putParcelable(DetailActivityFragment.ARG_MOVIE, movie);

                        DetailActivityFragment fragment = new DetailActivityFragment();
                        fragment.setArguments(args);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(
                                R.id.movie_detail_container);

                        getSupportFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                    }
                }
            }, 10);
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivityFragment.ARG_MOVIE, movie);
            startActivity(intent);
        }
    }
}
