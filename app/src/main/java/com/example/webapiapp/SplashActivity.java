package com.example.webapiapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private ApiInterface splashApiInterface;
    private boolean result;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        splashApiInterface = ApiUtils.getApi();

        myTask();
    }

    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://google.com");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {

                        result =  true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    result =  false;
                }

                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {

                        if (!result) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                            builder.setMessage("Bağlantınızı kontrol edip tekrar deneyiniz");
                            builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    myTask();
                                }
                            });
                            builder.setNegativeButton("Çıkış", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                        } else {
                            sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                            editor = sharedPreferences.edit();
                            int userNo = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));


                            Intent intentLogin = new Intent(SplashActivity.this,Login.class);
                            Intent intentMain = new Intent(SplashActivity.this,MainActivity.class);

                            if(userNo == 0){
                                startActivity(intentLogin);
                                finish();
                            }else{
                                try {
                                    User userSplash= new User(userNo,"","","","",null,"" );
                                    splashApiInterface.chekTimeOutloginUser(userSplash).enqueue(new Callback<Integer>() {
                                        @Override
                                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                                            if(response.isSuccessful()){
                                                int checkUserTimeOut = response.body();
                                                if (checkUserTimeOut == 1) {
                                                    startActivity(intentMain);
                                                    finish();
                                                }
                                                else{
                                                    startActivity(intentLogin);
                                                    finish();
                                                }
                                            }else{

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Integer> call, Throwable t) {

                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });
            }
        }).start();
    }
}
