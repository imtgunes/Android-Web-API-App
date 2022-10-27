package com.example.webapiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private ApiInterface cartApiInterface;

    private ArrayList<Cart> arrayListCart;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private Button saveToDoList,showNote;
    private TextView textView;
    private String toDoNote = "";
    private int shoppingCartID = 0;
    private int userID;

    private LocalDateTime timeNow;

    private Boolean isCartEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        cartApiInterface = ApiUtils.getApi();
        userID = getIntent().getIntExtra("userID",0);
        shoppingCartID = getIntent().getIntExtra("shoppingCartID",0);

        textView = findViewById(R.id.textViewCartEmpty);
        textView.setVisibility(View.INVISIBLE);
        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.materialToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        arrayListCart = new ArrayList<>();
        getCart();

        showNote = findViewById(R.id.buttonNote);
        showNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CartActivity.this,R.style.MyTransparentBottomSheetDialogTheme);
                bottomSheetDialog.setContentView(R.layout.bottom_dialog);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                EditText editTextCartNoteList= bottomSheetDialog.findViewById(R.id.editTextCartNoteList);
                ImageButton imageButtonExitBottom = bottomSheetDialog.findViewById(R.id.imageButtonExitBottom);
                imageButtonExitBottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editTextCartNoteList.setText("");
                        bottomSheetDialog.cancel();
                    }
                });
                Button buttonAddToCartNoteBottom = bottomSheetDialog.findViewById(R.id.buttonAddToCartNoteBottom);
                buttonAddToCartNoteBottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toDoNote = String.valueOf(editTextCartNoteList.getText());
                        bottomSheetDialog.cancel();

                    }
                });

                if(bottomSheetDialog.getWindow() != null)
                    bottomSheetDialog.getWindow().setDimAmount(0);
                bottomSheetDialog.show();
            }
        });

        saveToDoList = findViewById(R.id.buttonSaveToDo);
        saveToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCartEmpty){
                    myTask();
                }
                else{
                    Snackbar.make(view,"Sepet boş lütfen ürün ekleyiniz",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                addToDoList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();
    }

    public void addToDoList(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeNow = LocalDateTime.now(
            );
        }
        ToDo toDo = new ToDo(0,shoppingCartID,String.valueOf(timeNow),userID,0);
        try {
            cartApiInterface.addToDo(toDo).execute();
            ShoppingCart shoppingCart = new ShoppingCart(shoppingCartID,toDoNote,1,userID);
            cartApiInterface.updateShoppingCart(shoppingCartID,shoppingCart).execute();
            View view = findViewById(android.R.id.content);
            Snackbar.make(view,"Liste kaydedildi",Snackbar.LENGTH_SHORT).show();
            thread.start();

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content),"Liste kaydedilirken hata oluştu",Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                CartActivity.this.finish();
                Intent intent = new Intent(CartActivity.this,MainActivity.class);
                startActivity(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public void getCart(){
        cartApiInterface.getShoppingCartActivity(shoppingCartID).enqueue(new Callback<ArrayList<Cart>>() {
            @Override
            public void onResponse(Call<ArrayList<Cart>> call, Response<ArrayList<Cart>> response) {
                if(response.isSuccessful()){
                    ArrayList<Cart> carts = response.body();
                    if(carts.isEmpty()){
                        isCartEmpty = true;
                        View view = findViewById(android.R.id.content);
                        Snackbar.make(view,"Sepet boş lütfen ürün ekleyiniz",Snackbar.LENGTH_SHORT).show();
                    }else{
                        isCartEmpty = false;
                        for (Cart cart: carts){
                            arrayListCart.add(new Cart(cart.getShoppingCartsID(),cart.getShoppingCartsCartID(),cart.getShoppingCartsPiece(),cart.getShoppingCartsNote(),cart.getProductID(),cart.getProduct()));
                        }

                        adapter = new CustomAdapter(arrayListCart);
                        recyclerView.setAdapter(adapter);
                    }
                }else{

                }


            }

            @Override
            public void onFailure(Call<ArrayList<Cart>> call, Throwable t) {

            }
        });

    }
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private ArrayList<Cart> productArrayList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewProductCartName;
            private final TextView textViewProductCartWeight;
            private final TextView textViewProductCartPiece;
            private final TextView textViewProductCartNote;
            private final ImageView imageViewProductCart;

            public ViewHolder(View view) {
                super(view);
                textViewProductCartName = view.findViewById(R.id.textViewCartName);
                textViewProductCartWeight = view.findViewById(R.id.textViewCartlWeight);
                textViewProductCartNote = view.findViewById(R.id.textViewCartNote);
                textViewProductCartPiece= view.findViewById(R.id.textViewCartPiece);
                imageViewProductCart = view.findViewById(R.id.imageViewCart);

            }
        }

        public CustomAdapter(ArrayList<Cart> itemLists) {
            productArrayList = itemLists;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Cart cart = productArrayList.get(position);
            holder.textViewProductCartName.setText(cart.getProduct().getProductName());
            holder.textViewProductCartWeight.setText(String.valueOf(cart.getProduct().getProductWeight())+" g");
            holder.textViewProductCartPiece.setText("Adet: "+String.valueOf(cart.getShoppingCartsPiece()));
            holder.textViewProductCartNote.setText(cart.getShoppingCartsNote());
            Picasso.get().load(cart.getProduct().getProductImage()).fit().centerCrop().into(holder.imageViewProductCart);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    builder.setMessage("Ürünü sepetten çıkarıyorsunuz");
                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            cartApiInterface.removeItemOnCart(cart.getShoppingCartsID()).enqueue(new Callback<ShoppingCarts>() {
                                @Override
                                public void onResponse(Call<ShoppingCarts> call, Response<ShoppingCarts> response) {
                                    if(response.isSuccessful()){
                                        arrayListCart.clear();
                                        getCart();
                                    }else{

                                    }

                                }

                                @Override
                                public void onFailure(Call<ShoppingCarts> call, Throwable t) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return productArrayList.size();
        }
    }
}