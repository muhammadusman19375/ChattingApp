package com.example.chattingapp.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Adapters.MessagesAdapter;
import com.example.chattingapp.Models.Message;
import com.example.chattingapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);


        messages = new ArrayList<>();

        String name=getIntent().getStringExtra("name");
        String profile=getIntent().getStringExtra("image");
        receiverUid=getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        binding.name.setText(name);
        Glide.with(ChatActivity.this)
                .load(profile)
                .into(binding.imageView);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()){
                        if(status.equals("offline")){
                            binding.status.setVisibility(View.GONE);
                        }
                        else{
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;


        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter = new MessagesAdapter(ChatActivity.this, messages,senderRoom,receiverRoom);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
                        binding.recyclerView.setLayoutManager(linearLayoutManager);
                        binding.recyclerView.setAdapter(adapter);
                        linearLayoutManager.scrollToPosition(messages.size()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();

                Date date = new Date();
                Message message = new Message(messageTxt, senderUid, date.getTime());

                binding.messageBox.setText("");

                String randomKey = database.getReference().push().getKey();

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });

                        HashMap<String,Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg" , message.getMessage());
                        lastMsgObj.put("lastMsgTime" , date.getTime());

                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                    }
                });

            }
        });


        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });

        final Handler handler = new Handler();

        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);

            }
            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("online");
                }
            };
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25){
            if(data != null){
                if(data.getData() !=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();


                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUri(filePath);

                                        binding.messageBox.setText("");

                                        String randomKey = database.getReference().push().getKey();

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });

                                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                                lastMsgObj.put("lastMsg" , message.getMessage());
                                                lastMsgObj.put("lastMsgTime" , date.getTime());

                                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
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
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}