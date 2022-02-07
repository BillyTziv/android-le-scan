package com.example.recyclerview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<User> usersList;
    private RecyclerView recyclerView;
    private recyclerAdapter.RecyclerViewClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        usersList = new ArrayList<>();

        setUserInfo();//passes random information into ArrayList
        setAdapter();//set Adapter for Recycle View
    }

    private void setAdapter() {//Create Instance of Recycle Adapter
        setOnClickListner();
        recyclerAdapter adapter = new recyclerAdapter(usersList, listener);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void setOnClickListner() {
        listener = new recyclerAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("username", usersList.get(position).getUsername());
                startActivity(intent);
            }
        };
    }

    private void setUserInfo(){
        usersList = new ArrayList<>();//?
        usersList.add(new User("Gaben"));
        usersList.add(new User("Newel"));
        usersList.add(new User("Water"));
        usersList.add(new User("Tuna"));
        usersList.add(new User("Gaben"));
        usersList.add(new User("Newel"));
        usersList.add(new User("Water"));
        usersList.add(new User("Tuna"));
    }
}