package com.example.julian.popularmovie.model;

public class ApiError {
    private long statusCode;
    private String statusMessage;

    public long getStatusCode()
    {
        return statusCode;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }
}
