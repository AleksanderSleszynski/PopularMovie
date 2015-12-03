package com.example.julian.popularmovie.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.julian.popularmovie.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if(savedInstanceState == null){
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

}
