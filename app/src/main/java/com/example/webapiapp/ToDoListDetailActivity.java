package com.example.webapiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToDoListDetailActivity extends AppCompatActivity {
    private ApiInterface todoDetailApiInterface;

    private ArrayList<Cart> arrayListToDoDetail;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private int shoppingCartID = 0;
    private String shoppingCartNote = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_detail);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        shoppingCartID = getIntent().getIntExtra("shoppingCartID",0);

        todoDetailApiInterface = ApiUtils.getApi();

        recyclerView = findViewById(R.id.recyclerViewToDoDetail);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.materialToolbar4);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        arrayListToDoDetail = new ArrayList<>();
        getNote();
        getTodoDetail();
    }

    public void getTodoDetail(){
        todoDetailApiInterface.getShoppingCartActivity(shoppingCartID).enqueue(new Callback<ArrayList<Cart>>() {
            @Override
            public void onResponse(Call<ArrayList<Cart>> call, Response<ArrayList<Cart>> response) {
                if(response.isSuccessful()){
                    ArrayList<Cart> carts = response.body();
                    if(carts.isEmpty()){
                        Toast.makeText(ToDoListDetailActivity.this,"cart boş",Toast.LENGTH_SHORT).show();
                    }else{
                        for (Cart cart: carts){
                            arrayListToDoDetail.add(new Cart(cart.getShoppingCartsID(),cart.getShoppingCartsCartID(),cart.getShoppingCartsPiece(),cart.getShoppingCartsNote(),cart.getProductID(),cart.getProduct()));

                        }

                        adapter = new CustomAdapter(arrayListToDoDetail);
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


    public void getNote(){
        todoDetailApiInterface.getShoppingCartNote(shoppingCartID).enqueue(new Callback<ArrayList<ShoppingCart>>() {
            @Override
            public void onResponse(Call<ArrayList<ShoppingCart>> call, Response<ArrayList<ShoppingCart>> response) {
                if(response.isSuccessful()){
                    ArrayList<ShoppingCart> carts = response.body();
                    if(carts.isEmpty()){
                    }else{
                        for (ShoppingCart cart: carts){
                            shoppingCartNote = cart.getShoppingCartNote();

                        }

                    }
                }else{

                }

            }

            @Override
            public void onFailure(Call<ArrayList<ShoppingCart>> call, Throwable t) {

            }
        });
    }


    public class CustomAdapter extends RecyclerView.Adapter {
        private ArrayList<Cart> todoDetailItemLists;

        class ViewHolderOne extends RecyclerView.ViewHolder {

            private final TextView productName;
            private final TextView productWeight;
            private final TextView productPiece;
            private final TextView productNote;
            private final ImageView productImage;

            public ViewHolderOne(@NonNull View view) {
                super(view);
                productName = view.findViewById(R.id.textViewToDoDetailName);
                productImage = view.findViewById(R.id.imageViewToDoDetail);
                productWeight = view.findViewById(R.id.textViewToDoDetailWeight);
                productPiece = view.findViewById(R.id.textViewToDoDetailPiece);
                productNote = view.findViewById(R.id.textViewToDoDetailProductNote);
            }
        }

        class ViewHolderTwo extends RecyclerView.ViewHolder {
            private final TextView todoDetailNote;
            private final TextView productNoteHead;
            private final CardView cardViewTodoNote;
            public ViewHolderTwo(@NonNull View view) {
                super(view);
                todoDetailNote = view.findViewById(R.id.textViewToDoDetailToDoNote);
                productNoteHead = view.findViewById(R.id.textView3);
                cardViewTodoNote = view.findViewById(R.id.cardViewTodoNote);

            }
        }

        public CustomAdapter(ArrayList<Cart> itemLists) {
            todoDetailItemLists = itemLists;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount()-1) {
                return 0;
            }
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view;

            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.row_item_last, parent, false);
                return new ViewHolderTwo(view);
            }
            view = layoutInflater.inflate(R.layout.row_item_todo_detail, parent, false);
            return new ViewHolderOne(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (position == getItemCount()-1) {
                ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
                if(shoppingCartNote == " " || shoppingCartNote == null || shoppingCartNote.isEmpty()){
                    viewHolderTwo.todoDetailNote.setVisibility(View.GONE);
                    viewHolderTwo.productNoteHead.setVisibility(View.GONE);
                    viewHolderTwo.cardViewTodoNote.setVisibility(View.GONE);
                }else{
                    viewHolderTwo.todoDetailNote.setText(shoppingCartNote);
                }

            }else {
                final Cart cart = todoDetailItemLists.get(position);
                ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
                viewHolderOne.productName.setText(cart.getProduct().getProductName());
                viewHolderOne.productPiece.setText("Adet "+String.valueOf(cart.getShoppingCartsPiece()));
                viewHolderOne.productWeight.setText(cart.getProduct().getProductWeight().toString()+" g");
                viewHolderOne.productNote.setText(cart.getShoppingCartsNote());
                Picasso.get().load(cart.getProduct().getProductImage()).fit().centerCrop().into(viewHolderOne.productImage);
            }
        }

        @Override
        public int getItemCount() {
            return todoDetailItemLists.size()+1;
        }

    }
}