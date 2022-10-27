package com.example.webapiapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ApiInterface categoryApiInterface;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private ArrayList<Category> arrayListCategory;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private int shoppingCartID = 0;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int userID;

    private TextView textViewUserName, textViewUserSurName ;
    private ImageView imageViewUserImage ;
    private ImageButton imageButtonUserLogOut;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
        userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));

        categoryApiInterface = ApiUtils.getApi();

        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawer);

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.nav_open,R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.itemCart){
                    getShoppingCartID(userID);

                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }

                }
                else if(item.getItemId() == R.id.itemTodoList){
                    Intent intent = new Intent(MainActivity.this,ToDoActivity.class);
                    intent.putExtra("userID",userID);
                    startActivity(intent);
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                else if(item.getItemId() == R.id.itemTodoListComplated){
                    Intent intent = new Intent(MainActivity.this,ToDoActivityComplated.class);
                    intent.putExtra("userID",userID);
                    startActivity(intent);
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        getUserInfo(userID);
        imageButtonUserLogOut = headerView.findViewById(R.id.imageButtonUserLogOut);

        imageButtonUserLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("userNo","GuZMgQ2zRFt6sFV53NLtnA==");
                editor.commit();
                Intent intent = new Intent(MainActivity.this,SplashActivity.class);
                startActivity(intent);
                finish();

            }
        });

      //  toolbar.setNavigationIcon(R.drawable.ic_baseline_sort_24);

        recyclerView = findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        arrayListCategory = new ArrayList<>();

        getCategory();

    }

    public void getUserInfo(int userID){
        categoryApiInterface.getUserInfo(userID).enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if(response.isSuccessful()){
                    ArrayList<User> users = response.body();
                    String userName = "";
                    String userSurName = "";
                    String userImage = "";
                    for (User user: users){
                        userName = user.getUserName();
                        userSurName = user.getUserSurName();
                        userImage = user.getUserProfileImage();
                    }
                    View headerView = navigationView.getHeaderView(0);


                    textViewUserName = headerView.findViewById(R.id.textViewUserName);
                    textViewUserSurName = headerView.findViewById(R.id.textViewUserSurName);
                    textViewUserName.setText(userName);
                    textViewUserSurName.setText(userSurName);
                    imageViewUserImage = headerView.findViewById(R.id.imageViewUserImage);
                    Glide.with(MainActivity.this)
                            .asBitmap()
                            .load(userImage)
                            .centerCrop()
                            .into(imageViewUserImage);
                }else{

                }

            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {

            }
        });
    }
    public void getCategory(){
        categoryApiInterface.categories().enqueue(new Callback<ArrayList<Category>>() {
            @Override
            public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                if(response.isSuccessful()){
                    ArrayList<Category> categories = response.body();
                    for (Category category: categories){
                        arrayListCategory.add(new Category(category.getCategoryID(),category.getCategoryName(),category.getCategoryImage()));

                    }

                    adapter = new CustomAdapter(arrayListCategory);
                    recyclerView.setAdapter(adapter);
                }else{

                }


            }

            @Override
            public void onFailure(Call<ArrayList<Category>> call, Throwable t) {

            }
        });
    }

    public void getShoppingCartID(int userID){
        categoryApiInterface.getShoppingCartID(userID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful()){
                    shoppingCartID = response.body();
                    Intent intent = new Intent(MainActivity.this,CartActivity.class);
                    intent.putExtra("shoppingCartID",shoppingCartID);
                    startActivity(intent);
                }else{

                }

            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private ArrayList<Category> categoryArrayList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView categoryName;
            private final ImageView categoryImage;

            public ViewHolder(View view) {
                super(view);
                categoryName = view.findViewById(R.id.textViewCategoryItem);
                categoryImage = view.findViewById(R.id.imageViewCategoryImage);

            }
        }

        public CustomAdapter(ArrayList<Category> itemLists) {
            categoryArrayList = itemLists;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Category category = categoryArrayList.get(position);
            holder.categoryName.setText(category.getCategoryName());
            Picasso.get().load(category.getCategoryImage()).fit().centerCrop().into(holder.categoryImage);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,ProductActivity.class);
                    intent.putExtra("categoryID",category.getCategoryID());
                    intent.putExtra("userID",userID);
                    startActivity(intent);

                }
            });
        }

        @Override
        public int getItemCount() {
            return categoryArrayList.size();
        }
    }
}