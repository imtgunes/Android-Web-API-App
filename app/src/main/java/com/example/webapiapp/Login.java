package com.example.webapiapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private ApiInterface loginApiInterface;

    private Button buttonLogin;
    private EditText editTextTextLoginEmailAddress, editTextTextLoginPassword;
    private TextView textViewRegister;
    
    private int userID;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginApiInterface = ApiUtils.getApi();

        buttonLogin = findViewById(R.id.buttonLogin);
        editTextTextLoginEmailAddress = findViewById(R.id.editTextTextLoginEmailAddress);
        editTextTextLoginPassword = findViewById(R.id.editTextTextLoginPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(Login.this,SignIn.class);
                startActivity(signIn);
                Login.this.finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editTextTextLoginEmailAddress.getText().toString())){
                    Snackbar.make(view,"E-posta boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextLoginPassword.getText().toString())){
                    Snackbar.make(view,"Şifre boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        User userLogin = new User(0,"","",editTextTextLoginPassword.getText().toString(),editTextTextLoginEmailAddress.getText().toString()
                                ,null,"" );
                        loginApiInterface.loginUser(userLogin).enqueue(new Callback<ArrayList<User>>() {
                            @Override
                            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                                ArrayList<User> user = response.body();
                                if(!user.isEmpty()){
                                    User userInfo = null;
                                    for (User userItem: user){
                                        userInfo = new User(userItem.getUserID(),userItem.getUserName(),userItem.getUserSurName(),userItem.getUserProfileImage());
                                    }
                                    if(userInfo.getUserID() == null && userInfo.getUserName().isEmpty()){
                                        Snackbar.make(view,"E-posta veya şifreniz hatalı",Snackbar.LENGTH_SHORT).show();
                                    }else{
                                        String userName = userInfo.getUserName();
                                        String userSurName = userInfo.getUserSurName();
                                        String userImage = userInfo.getUserProfileImage();

                                        sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                                        editor = sharedPreferences.edit();
                                        editor.putString("userNo",Encryption.encrypt(String.valueOf(userInfo.getUserID())));
                                        editor.commit();

                                        Intent intent = new Intent(Login.this,MainActivity.class);
                                        startActivity(intent);
                                        Login.this.finish();
                                    }
                                }else{
                                    Snackbar.make(view,"E-posta veya şifreniz hatalı",Snackbar.LENGTH_SHORT).show();
                                }

                            }
                            @Override
                            public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                                Snackbar.make(view,"Bir hatta oluştu lütfen tekrar deneyiniz",Snackbar.LENGTH_SHORT).show();
                            }
                        });


                    }catch (Exception e){
                        Snackbar.make(view,"Bir hatta oluştu lütfen tekrar deneyiniz",Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}