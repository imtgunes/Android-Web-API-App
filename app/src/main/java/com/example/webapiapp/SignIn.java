package com.example.webapiapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignIn extends AppCompatActivity {
    private ApiInterface signInApiInterface;

    private Button buttonSignIn;
    private EditText editTextTextSignInName, editTextTextSignInSurName, editTextTextSignInEmailAddress, editTextTextSignInPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signInApiInterface = ApiUtils.getApi();

        buttonSignIn = findViewById(R.id.buttonSignIn);
        editTextTextSignInName = findViewById(R.id.editTextTextSignInName);
        editTextTextSignInSurName = findViewById(R.id.editTextTextSignInSurName);
        editTextTextSignInEmailAddress = findViewById(R.id.editTextTextSignInEmailAddress);
        editTextTextSignInPassword = findViewById(R.id.editTextTextSignInPassword);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editTextTextSignInName.getText().toString())){
                    Snackbar.make(view,"Ad boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInSurName.getText().toString())){
                    Snackbar.make(view,"Soyad boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInEmailAddress.getText().toString())){
                    Snackbar.make(view,"E-posta boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInPassword.getText().toString())){
                    Snackbar.make(view,"Şifre boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        User user = new User(0,editTextTextSignInName.getText().toString(),editTextTextSignInSurName.getText().toString(),editTextTextSignInPassword.getText().toString(),editTextTextSignInEmailAddress.getText().toString(),null,"");
                        signInApiInterface.signUser(user).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                int checkUserSignIn = response.body();
                                if (checkUserSignIn == 1) {
                                    Snackbar.make(view,"Kayıt olundu",Snackbar.LENGTH_SHORT).show();
                                    thread.start();
                                }else if(checkUserSignIn == 2){
                                    Snackbar.make(view,"Bu mail ile üyelik bulunmaktadır",Snackbar.LENGTH_SHORT).show();
                                }
                                else{
                                    Snackbar.make(view,"Kayıt olunurken bir hatayla karşılaşıldı",Snackbar.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {

                            }
                        });


                    }catch (Exception e){
                        System.out.println("Exception"+e);
                    }
                }

            }
        });

    }

    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                SignIn.this.finish();
                Intent intent = new Intent(SignIn.this,Login.class);
                startActivity(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}