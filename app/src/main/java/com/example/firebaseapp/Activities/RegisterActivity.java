package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.example.firebaseapp.Models.userInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    TextView registerMe;
    EditText userName,userUserName,userEmail, userPassword;
    TextView alreadyHaveAccount;
    Intent intent;
    String email, password;
    ImageView profileImage;
    //progressBar to display while registering user
    ProgressDialog progressDialog;
    //UserInformation Class
    com.example.firebaseapp.Models.userInformation userInformation = new userInformation();

    /*back Button*/
    ImageView sback;
    /*Firebase*/
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }
    public void init() {
        registerMe = findViewById(R.id.registerMe);
        userName=   findViewById(R.id.nameEditText);
        userUserName=   findViewById(R.id.UsernameEditText);
        userEmail = findViewById(R.id.emailEditText);
        userPassword = findViewById(R.id.passwordEditText);
        alreadyHaveAccount=findViewById(R.id.haveAccount);
        progressDialog = new ProgressDialog(this);
        registerMe.setOnClickListener(this);
        alreadyHaveAccount.setOnClickListener(this);

        /*back Button*/
        sback = (ImageView)findViewById(R.id.sback);
        sback.setOnClickListener(this);
        /*profileImage*/
        profileImage=findViewById(R.id.addProfileImage);
        profileImage.setOnClickListener(this);

        /*FirebaseAuth*/
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerMe:
                email = userEmail.getText().toString();
                password = userPassword.getText().toString();
                //setting user information to userInformation Class
                userInformation = new userInformation(email, password);
                if (userInformation.getUserEmail().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userInformation.getUserEmail()).matches()) {
                    userEmail.setError("Invalid Email");
                    userEmail.requestFocus();
                } else if (userInformation.getUserPassword().isEmpty() || userInformation.getUserPassword().length()<6) {
                    userPassword.setError("Enter Password at least 6 Characters");
                    userPassword.requestFocus();
                } else {
                    registerUser();
                }
                break;
            case R.id.haveAccount:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.sback:
                startActivity(new Intent(this,MainActivity.class));
                break;
        }
    }
    public void registerUser(){
        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(userInformation.getUserEmail(), userInformation.getUserPassword()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            emailVerification();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void emailVerification(){
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Verify Email Address...", Toast.LENGTH_SHORT).show();
                    //get user email and uid from auth
                    String email = firebaseAuth.getCurrentUser().getEmail();
                    String uid= firebaseAuth.getUid();
                    String name=userName.getText().toString();
                    String Username=userUserName.getText().toString();
                    //when user is registered to store data in firebase realtime database
                    //using hashmap
                    HashMap<Object, String> hashMap=new HashMap<>();
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name",name);
                    hashMap.put("Username",Username);
                    hashMap.put("onlineStatus","online");
                    hashMap.put("typingTo","noOne");
                    hashMap.put("phone","");
                    hashMap.put("profileImage","");
                    hashMap.put("coverImage","");
                    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                    //path to store user data name "Users"
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Users");
                    //putting data within hashmap in database
                    databaseReference.child(uid).setValue(hashMap);
                    //Toast.makeText(RegisterActivity.this, "Registered:"+userInformation.getUserEmail(), Toast.LENGTH_SHORT).show();
                    intent=new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
