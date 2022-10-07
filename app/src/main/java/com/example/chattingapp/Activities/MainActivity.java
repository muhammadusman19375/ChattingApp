package com.example.chattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chattingapp.R;
import com.example.chattingapp.Adapters.UsersAdapter;
import com.example.chattingapp.databinding.ActivityMainBinding;
import com.example.chattingapp.Models.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<user> users;
    UsersAdapter usersAdapter;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    user user=snapshot1.getValue(com.example.chattingapp.Models.user.class);
                    if (user.getUid().equals(auth.getUid())){
//                        Toast.makeText(MainActivity.this, auth.getCurrentUser() ,Toast.LENGTH_SHORT).show();
                    }else{
                        users.add(user);
//                        Toast.makeText(MainActivity.this, user.getName(), Toast.LENGTH_SHORT).show();

                    }
                    usersAdapter = new UsersAdapter(MainActivity.this,users);

                   LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(MainActivity.this);
                    binding.recyclerView.setLayoutManager(mLinearLayoutManager);
                    binding.recyclerView.setAdapter(usersAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("offline");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(MainActivity.this, "Search clicked.", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}