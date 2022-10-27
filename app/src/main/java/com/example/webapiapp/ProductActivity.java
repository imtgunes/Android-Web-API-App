package com.example.webapiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity {
    private ApiInterface productApiInterface;

    private ArrayList<Product> arrayListProduct;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FrameLayout frameLayout;
    private Fragment fragmentAddToCart;
    private FragmentManager fragmentManager;

    private int userID;
    private int categoryID;
    private int shoppingCartID = 0;

    private SearchView searchView;

    private ImageButton imageButtonBasket;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        userID = getIntent().getIntExtra("userID",0);
        categoryID = getIntent().getIntExtra("categoryID",0);

        productApiInterface = ApiUtils.getApi();

        recyclerView = findViewById(R.id.recyclerViewProduct);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        frameLayout = findViewById(R.id.frameLayaut);
        fragmentManager = getSupportFragmentManager();
        fragmentAddToCart = new FragmentAddToCart();

        Toolbar toolbar = findViewById(R.id.materialToolbar3);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        arrayListProduct = new ArrayList<>();
        getProdcut(categoryID);

        getShoppingCartID(userID);

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Search(newText);
                return false;
            }
        });

        imageButtonBasket = findViewById(R.id.imageButtonProductBasket);
        imageButtonBasket.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductActivity.this,CartActivity.class);
                intent.putExtra("shoppingCartID",shoppingCartID);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });


    }

    public void getProdcut(int category){
        productApiInterface.products(category).enqueue(new Callback<ArrayList<Product>>() {
            @Override
            public void onResponse(Call<ArrayList<Product>> call, Response<ArrayList<Product>> response) {
                if(response.isSuccessful()){
                    ArrayList<Product> categories = response.body();
                    for (Product product: categories){
                        arrayListProduct.add(new Product(product.getProductID(),product.getProductName(),product.getProductCategoryID(),product.getProductImage(),product.getProductWeight()));

                    }

                    adapter = new CustomAdapter(arrayListProduct);
                    recyclerView.setAdapter(adapter);
                }else{

                }


            }

            @Override
            public void onFailure(Call<ArrayList<Product>> call, Throwable t) {
                System.out.println("hataaaaaaaaaaaaaaaaaa"+t);
            }
        });

    }

    public void getShoppingCartID(int userID){
        productApiInterface.getShoppingCartID(userID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful()){
                    shoppingCartID = response.body();
                }else{

                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }

    private void Search(String productName){
        ArrayList<Product> filteredArrayList = new ArrayList<>();

        for(Product product : arrayListProduct){
            if(product.getProductName().toLowerCase().contains(productName.toLowerCase())){
                filteredArrayList.add(product);
            }
            if (filteredArrayList.isEmpty()) {
                filteredArrayList.clear();
                adapter.filterList(filteredArrayList);
            } else {
                adapter.filterList(filteredArrayList);
            }

        }
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private ArrayList<Product> productArrayList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewProductItemName;
            private final TextView textViewProductItemWeight;
            private final ImageView imageViewProductItem;

            public ViewHolder(View view) {
                super(view);
                textViewProductItemName = view.findViewById(R.id.textViewProductItemName);
                textViewProductItemWeight = view.findViewById(R.id.textViewProductItemWeight);
                imageViewProductItem = view.findViewById(R.id.imageViewProductItem);

            }
        }

        public CustomAdapter(ArrayList<Product> itemLists) {
            productArrayList = itemLists;
        }

        public void filterList(ArrayList<Product> filteredlist) {
            this.productArrayList = filteredlist;

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Product product = productArrayList.get(position);
            holder.textViewProductItemName.setText(product.getProductName());
            holder.textViewProductItemWeight.setText(String.valueOf(product.getProductWeight())+" g");
            Picasso.get().load(product.getProductImage()).fit().centerCrop().into(holder.imageViewProductItem);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(fragmentAddToCart.isAdded()){
                        ProductActivity.this.getFragmentManager().popBackStack();
                        ProductActivity.this.onBackPressed();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.addToBackStack(null);
                        Bundle bundle = new Bundle();
                        bundle.putInt("userID",userID);
                        bundle.putInt("shoppingCartID",shoppingCartID);
                        bundle.putInt("productID",product.getProductID());
                        bundle.putString("productName",product.getProductName());
                        bundle.putString("productImage",product.getProductImage());
                        bundle.putFloat("productWeight",product.getProductWeight());
                        fragmentAddToCart.setArguments(bundle);
                        fragmentTransaction.add(R.id.frameLayaut,fragmentAddToCart);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        frameLayout.setVisibility(View.VISIBLE);
                        return;
                    }

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    Bundle bundle = new Bundle();
                    bundle.putInt("userID",userID);
                    bundle.putInt("shoppingCartID",shoppingCartID);
                    bundle.putInt("productID",product.getProductID());
                    bundle.putString("productName",product.getProductName());
                    bundle.putString("productImage",product.getProductImage());
                    bundle.putFloat("productWeight",product.getProductWeight());
                    fragmentAddToCart.setArguments(bundle);
                    fragmentTransaction.add(R.id.frameLayaut,fragmentAddToCart);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    frameLayout.setVisibility(View.VISIBLE);

                }
            });
        }

        @Override
        public int getItemCount() {
            return productArrayList.size();
        }
    }
}