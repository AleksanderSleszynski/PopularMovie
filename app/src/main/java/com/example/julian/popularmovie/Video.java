package com.example.julian.popularmovie;


import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable{

    public static final String SITE_YOUTUBE = "YouTube";

    private String site;
    private String key;

    public boolean isValid(){
        return SITE_YOUTUBE.equals(site) && key != null && key.length() > 0;
    }

    public Video(String site, String key){
        this.site = site;
        this.key  = key;
    }

    protected Video(Parcel in) {
        site = in.readString();
        key  = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(site);
        dest.writeString(key);
    }
}
