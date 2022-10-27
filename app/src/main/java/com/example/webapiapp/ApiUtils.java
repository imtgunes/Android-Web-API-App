package com.example.webapiapp;

public class ApiUtils {
    public static final String BASE_URL = "https://b6af-195-177-206-66.eu.ngrok.io/";

    public static ApiInterface getApi(){
        return  ApiClient.getRetrofit(BASE_URL).create(ApiInterface.class);
    }
}
