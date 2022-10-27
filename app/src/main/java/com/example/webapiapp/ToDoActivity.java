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
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToDoActivity extends AppCompatActivity {
    private ApiInterface todoApiInterface;

    private ArrayList<ToDo> arrayListToDo;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        todoApiInterface = ApiUtils.getApi();
        
        userID = getIntent().getIntExtra("userID",0);

        recyclerView = findViewById(R.id.recyclerViewToDoList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.materialToolbar2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        arrayListToDo = new ArrayList<>();
        getTodo();

    }
    public void getTodo(){
        todoApiInterface.getTodoNotComplated(userID,0).enqueue(new Callback<ArrayList<ToDo>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<ArrayList<ToDo>> call, Response<ArrayList<ToDo>> response) {
                if(response.isSuccessful()){
                    ArrayList<ToDo> toDos = response.body();
                    for (ToDo toDo: toDos){
                        arrayListToDo.add(new ToDo(toDo.getTodoID(), toDo.getTodoShoppingCartID(), toDo.getTodoSaveDate(), toDo.getTodoUserID(), toDo.getTodoState()));

                    }

                    adapter = new CustomAdapter(arrayListToDo);
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
            private final ImageButton imageButtonToDoComplate;
            private final ImageButton imageButtonToDoDelete;
            public ViewHolder(View view) {
                super(view);
                textViewToDoNumber = view.findViewById(R.id.textViewToDoNumber);
                textViewToDoDate = view.findViewById(R.id.textViewToDoDate);
                imageButtonToDoComplate = view.findViewById(R.id.imageButtonToDoComplate);
                imageButtonToDoDelete= view.findViewById(R.id.imageButtonToDoDelete);

            }
        }

        public CustomAdapter(ArrayList<ToDo> itemLists) {
            todoArrayList = itemLists;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_todo, parent, false);
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
                    Intent intent = new Intent(ToDoActivity.this,ToDoListDetailActivity.class);
                    intent.putExtra("shoppingCartID",toDo.getTodoShoppingCartID());
                    startActivity(intent);
                }
            });
            holder.imageButtonToDoComplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toDo.setTodoState(1);
                    todoApiInterface.updateToDo(toDo.getTodoID(),toDo).enqueue(new Callback<ToDo>() {
                        @Override
                        public void onResponse(Call<ToDo> call, Response<ToDo> response) {
                            if(response.isSuccessful()){
                                Snackbar.make(view,"Liste tamamlandı",Snackbar.LENGTH_SHORT).show();
                                arrayListToDo.clear();
                                getTodo();
                            }else{

                            }

                        }

                        @Override
                        public void onFailure(Call<ToDo> call, Throwable t) {
                            Snackbar.make(view,"Liste tamamlandı olark işaretlenirken hata oluştu",Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            holder.imageButtonToDoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ToDoActivity.this);
                    builder.setMessage("Liste siliniyor");
                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Snackbar.make(view,"Liste silindi",Snackbar.LENGTH_SHORT).show();
                            todoApiInterface.deleteShoppingCart(toDo.getTodoShoppingCartID()).enqueue(new Callback<ShoppingCart>() {
                                @Override
                                public void onResponse(Call<ShoppingCart> call, Response<ShoppingCart> response) {
                                    if(response.isSuccessful()){
                                        Snackbar.make(view,"Liste silindi",Snackbar.LENGTH_SHORT).show();
                                        arrayListToDo.clear();
                                        getTodo();
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