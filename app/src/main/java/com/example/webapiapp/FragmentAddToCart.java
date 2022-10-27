package com.example.webapiapp;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.PreparedStatement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAddToCart extends Fragment {
    private ApiInterface fragmentApiInterface;

    private ImageButton imageButtonExit;
    private Button buttonAddToCart;
    private ImageView imageViewPiecePlus,imageViewPieceMinus,imageViewProductImage;
    private TextView textViewPieceFragment,textViewProductNameFragment,textViewProductWeight;
    private EditText editTextNote;
    private int productPiece;
    private String cartNote;
    private int shoppingCartID;
    private int productID;
    private int userID;


    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_add_to_cart, container, false);
        userID = this.getArguments().getInt("userID",0);
        productID = this.getArguments().getInt("productID",0);
        shoppingCartID = this.getArguments().getInt("shoppingCartID",0);
        String productName = this.getArguments().getString("productName");
        String prodcutImage = this.getArguments().getString("productImage");
        Float productWeight = this.getArguments().getFloat("productWeight");
        cartNote = "";
        productPiece = 1;

        fragmentApiInterface = ApiUtils.getApi();

        imageButtonExit = view.findViewById(R.id.imageButtonExit);
        imageViewPiecePlus = view.findViewById(R.id.imageViewPlusFragment);
        imageViewPieceMinus = view.findViewById(R.id.imageViewMinusFragment);
        textViewPieceFragment = view.findViewById(R.id.textViewProductPieceFragment);
        textViewProductNameFragment = view.findViewById(R.id.textViewProductNameFragment);
        imageViewProductImage = view.findViewById(R.id.imageViewToDoDetail);
        buttonAddToCart = view.findViewById(R.id.buttonAddToCartFragment);
        textViewProductWeight = view.findViewById(R.id.textViewProductWeightFragment);
        editTextNote = view.findViewById(R.id.editTextTextNoteFragment);

        textViewProductNameFragment.setText(productName);
        textViewProductWeight.setText(String.valueOf(productWeight)+" g");

        Picasso.get().load(prodcutImage).into(imageViewProductImage);
        Activity activity = getActivity();

        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                cartNote = String.valueOf(editTextNote.getText());
                Cart cart = new Cart(shoppingCartID,productPiece,cartNote,productID,null);
                fragmentApiInterface.addShoppingCarts(cart).enqueue(new Callback<Cart>() {
                    @Override
                    public void onResponse(Call<Cart> call, Response<Cart> response) {

                    }

                    @Override
                    public void onFailure(Call<Cart> call, Throwable t) {

                    }
                });

                 Snackbar snackbar = Snackbar.make(view,"Ürün eklendi ",Snackbar.LENGTH_SHORT)
                        .setAction("Sepete Git", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(activity, CartActivity.class);
                                intent.putExtra("shoppingCartID",shoppingCartID);
                                intent.putExtra("userID",userID);
                                activity.startActivity(intent);
                            }
                        });
                snackbar.show();

                editTextNote.setText("");
                productPiece = 1;
                getActivity().getFragmentManager().popBackStack();
                hideSoftKeyboard(getActivity());
                getActivity().onBackPressed();
            }
        });

        imageViewPiecePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productPiece = Integer.parseInt(textViewPieceFragment.getText().toString());

                productPiece++;
                textViewPieceFragment.setText(String.valueOf(productPiece));

            }
        });

        imageViewPieceMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productPiece = Integer.parseInt(textViewPieceFragment.getText().toString());
                if(productPiece > 1){
                    productPiece--;
                    textViewPieceFragment.setText(String.valueOf(productPiece));
                }


            }
        });

        imageButtonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextNote.setText("");
                hideSoftKeyboard(getActivity());
                getActivity().getFragmentManager().popBackStack();
                getActivity().onBackPressed();

            }
        });

        return view;
    }
    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}