package com.example.rs_chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.rs_chattingapp.Adapter.ChatAdapter;
import com.example.rs_chattingapp.databinding.ActivityChatDetailBinding;
import com.example.rs_chattingapp.models.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        getSupportActionBar().hide();
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        final String senderID=auth.getUid();
        String receiveid=getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("username");
        String profilepic=getIntent().getStringExtra("ProfilePic");
        binding.userName.setText(userName);
        Picasso.get().load(profilepic).placeholder(R.drawable.ic_baseline_face_24).into(binding.profileImage);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this,receiveid);
        binding.ChatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        binding.ChatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderID+receiveid;
        final String receiverRoom = receiveid+senderID;

        database.getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModels.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    MessageModel model = snapshot1.getValue(MessageModel.class);
                    model.setMessageId(snapshot1.getKey());
                    messageModels.add(model);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 String message=binding.etmsg.getText().toString();
                 final MessageModel model =new MessageModel(senderID,message);
                 model.setTimestamp(new Date().getTime());
                 binding.etmsg.setText("");
                 database.getReference().child("Chats").child(senderRoom).push().setValue(model)
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         database.getReference().child("chats").child(receiverRoom).push()
                                 .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {

                             }
                         });
                     }
                 });
            }
        });

    }
}