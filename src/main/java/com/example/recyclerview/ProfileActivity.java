package com.example.recyclerview;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ArrayList<Device> userList;
    private recyclerAdapter.RecyclerViewClickListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        TextView nameTxt = findViewById(R.id.nameTextView);

        getSupportActionBar().hide();

        String username = "Username not set";

        Bundle extras = getIntent().getExtras();

        if( extras != null) {
            username = extras.getString("username");
        }

        nameTxt.setText(username);
    }
}
