package com.example.julian.popularmovie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Review implements Parcelable{

    private String author;
    private String content;

    public Review(String author, String content){
        this.author  = author;
        this.content = content;
    }

    public Review() {

    }

    public boolean isValid() {
        return !TextUtils.isEmpty(author) && !TextUtils.isEmpty(content);
    }

    protected Review(Parcel in) {
        author  = in.readString();
        content = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
