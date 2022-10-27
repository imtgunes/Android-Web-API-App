package com.example.webapiapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToDoActivityComplated extends AppCompatActivity {
    private ApiInterface todoComplatedApiInterface;

    private ArrayList<ToDo> arrayListToDoComplated;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_complated);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        todoComplatedApiInterface = ApiUtils.getApi();

        userID = getIntent().getIntExtra("userID",0);

        recyclerView = findViewById(R.id.recyclerViewTodoComplated);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.materialToolbar5);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        arrayListToDoComplated = new ArrayList<>();
        getTodoComplated();
    }
    public void getTodoComplated(){
        todoComplatedApiInterface.getTodoNotComplated(userID,1).enqueue(new Callback<ArrayList<ToDo>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<ArrayList<ToDo>> call, Response<ArrayList<ToDo>> response) {
                if(response.isSuccessful()){
                    ArrayList<ToDo> toDos = response.body();
                    for (ToDo toDo: toDos){

                        arrayListToDoComplated.add(new ToDo(toDo.getTodoID(), toDo.getTodoShoppingCartID(), toDo.getTodoSaveDate(), toDo.getTodoUserID(), toDo.getTodoState()));

                    }

                    adapter = new CustomAdapter(arrayListToDoComplated);
                    recyclerView.setAdapter(adapter);
                }else{

                }

            }

            @Override
            public void onFailure(Call<ArrayList<ToDo>> call, Throwable t) {

            }
        });

    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private ArrayList<ToDo> todoArrayList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewToDoNumber;
            private final TextView textViewToDoDate;
            private final ImageButton imageButtonToDoDelete;
            public ViewHolder(View view) {
                super(view);
                textViewToDoNumber = view.findViewById(R.id.textViewToDoNumberComplated);
                textViewToDoDate = view.findViewById(R.id.textViewToDoDateComplated);
                imageButtonToDoDelete= view.findViewById(R.id.imageButtonToDoDeleteComplated);

            }
        }

        public CustomAdapter(ArrayList<ToDo> itemLists) {
            todoArrayList = itemLists;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_todo_complated, parent, false);
            return new ViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final ToDo toDo = todoArrayList.get(position);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            LocalDateTime localDateTime = LocalDateTime.parse(toDo.getTodoSaveDate().substring(0,16), dateTimeFormatter);

            String date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(localDateTime);
            holder.textViewToDoNumber.setText("Liste "+String.valueOf(toDo.getTodoID()));
            holder.textViewToDoDate.setText(date);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ToDoActivityComplated.this,ToDoListDetailActivity.class);
                    intent.putExtra("shoppingCartID",toDo.getTodoShoppingCartID());
                    startActivity(intent);
                }
            });
            holder.imageButtonToDoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ToDoActivityComplated.this);
                    builder.setMessage("Liste siliniyor");
                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            todoComplatedApiInterface.deleteShoppingCart(toDo.getTodoShoppingCartID()).enqueue(new Callback<ShoppingCart>() {
                                @Override
                                public void onResponse(Call<ShoppingCart> call, Response<ShoppingCart> response) {
                                    if(response.isSuccessful()){
                                        Snackbar.make(view,"Liste silindi",Snackbar.LENGTH_SHORT).show();
                                        arrayListToDoComplated.clear();
                                        getTodoComplated();
                                    }else{

                                    }

                                }

                                @Override
                                public void onFailure(Call<ShoppingCart> call, Throwable t) {
                                    Snackbar.make(view,"Liste silinirken bir hata oluştu",Snackbar.LENGTH_SHORT).show();
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
            return todoArrayList.size();
        }
    }
}