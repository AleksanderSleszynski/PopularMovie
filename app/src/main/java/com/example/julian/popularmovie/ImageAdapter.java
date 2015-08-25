package com.example.julian.popularmovie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Julian on 8/23/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<Movie> posters;
    LayoutInflater inflater;

//    public static final String BASE_URL = "http://image.tmdb.org/t/p/";
//    public static final String IMAGE_SIZE = "w185";

    public ImageAdapter(Context context, ArrayList<Movie> posters) {
        this.context = context;
        this.posters = posters;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Movie movie){
        posters.add(movie);
        super.notifyDataSetChanged();
    }

    public void clear(){
        posters.clear();
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return posters.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(context).load(posters.get(position).getPosterUrl()).error(R.drawable.sample_0).into(imageView);

        return imageView;
    }

}
