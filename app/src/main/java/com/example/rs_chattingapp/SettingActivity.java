package com.example.rs_chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.view.View;
import android.widget.Toast;

import com.example.rs_chattingapp.databinding.ActivitySettingBinding;
import com.example.rs_chattingapp.models.users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;
    FirebaseStorage storage;
    FirebaseAuth mauth;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        storage = FirebaseStorage.getInstance();
        mauth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.backArrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        binding.savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = binding.etStatus.getText().toString();
                String username = binding.etUsername.getText().toString();

                HashMap<String , Object> obj = new HashMap<>();
                obj.put("username" , username);
                obj.put("status" , status);

                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).
                        updateChildren(obj);

                Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();


            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users users = snapshot.getValue(users.class);
                        Picasso.get().load(users.getProfilepic())
                                .into(binding.profileImage);

                        binding.etStatus.setText(users.getStatus());
                        binding.etUsername.setText(users.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("Images/*");
                startActivityForResult(intent,33);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData()!=null)
        {
            Uri sfile = data.getData();
            binding.profileImage.setImageURI(sfile);

            final StorageReference reference = storage.getReference().child("Profile Pictures").
                    child(FirebaseAuth.getInstance().getUid());
            reference.putFile(sfile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance()
                            .getUid()).child("Profile Pic").setValue(uri.toString());
                            Toast.makeText(SettingActivity.this, "Profile Pic Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}