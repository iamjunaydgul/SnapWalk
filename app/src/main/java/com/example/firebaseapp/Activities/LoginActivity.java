package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.example.firebaseapp.Models.userInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    TextView loginMe;
    EditText userEmail, userPassword, recoverPasswordEditText;
    TextView forgotPassword;
    Intent intent;
    String email, password;
    //progressBar to display while registering user
    ProgressDialog progressDialog;
    //UserInformation Class
    com.example.firebaseapp.Models.userInformation userInformation = new userInformation();
    //FirebaseAuthentication Setup
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    //Google Sign In Button
    TextView googleSignInButton;
    GoogleSignInClient mGoogleSignInClient;
    public static final int RC_SIGN_IN=100;

    /*back Button*/
    ImageView sback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
       /* ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);*/
        init();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
    }
    public void init() {
        loginMe = findViewById(R.id.loginMe);
        userEmail = findViewById(R.id.emailEditText);
        userPassword = findViewById(R.id.passwordEditText);
        //dontHaveAccount = findViewById(R.id.dontHaveAccount);
        forgotPassword = findViewById(R.id.forgotPasswordTextView);
        googleSignInButton=findViewById(R.id.googleSignInButton);
        progressDialog = new ProgressDialog(this);
        loginMe.setOnClickListener(this);
        //dontHaveAccount.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        googleSignInButton.setOnClickListener(this);
        //FirebaseAuth Instance
        firebaseAuth=FirebaseAuth.getInstance();
        /*image Back*/
        sback = (ImageView)findViewById(R.id.sinb);
        sback.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginMe:
                email = userEmail.getText().toString();
                password = userPassword.getText().toString();
                //setting user information to userInformation Class
                userInformation = new userInformation(email, password);
                if (userInformation.getUserEmail().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userInformation.getUserEmail()).matches()) {
                    userEmail.setError("Invalid Email");
                    userEmail.requestFocus();
                } else if (userInformation.getUserPassword().isEmpty() || userInformation.getUserPassword().length() < 6) {
                    userPassword.setError("Enter Password at least 6 Characters");
                    userPassword.requestFocus();
                } else {
                    loginUser();
                }
                break;
            case R.id.forgotPasswordTextView:
                showRecoverPasswordDialoge();
                break;
            case R.id.googleSignInButton:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.sinb:
                startActivity(new Intent(this,MainActivity.class));
                break;
        }
    }
    public void loginUser() {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(userInformation.getUserEmail(), userInformation.getUserPassword()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Successfully Login:" + userInformation.getUserEmail(), Toast.LENGTH_SHORT).show();
                                intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                startActivity(intent);
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,"Verify Email Address...", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void showRecoverPasswordDialoge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        recoverPasswordEditText = new EditText(this);
        recoverPasswordEditText.setHint("Enter your email...");
        recoverPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        recoverPasswordEditText.setMaxEms(16);
        recoverPasswordEditText.setWidth(900);
        linearLayout.addView(recoverPasswordEditText);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                recoverPassowrd();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public void recoverPassowrd() {
        progressDialog.setMessage("Sending Email...");
        progressDialog.show();
        if (!recoverPasswordEditText.getText().toString().isEmpty()) {
            String requestEmail = recoverPasswordEditText.getText().toString();
            //for resetting the password send an email to given email with resetting the password
            firebaseAuth.sendPasswordResetEmail(requestEmail).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Check Email!", Toast.LENGTH_SHORT).show();

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            recoverPasswordEditText.setError("Please enter email...");
            recoverPasswordEditText.requestFocus();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i("Google sign in failed", e.getMessage());
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i("firebaseAuthWithGoogle:", acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Sign in success, update UI with the signed-in user's information
                        //go to profile activity after logged in
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //if user is signing in first time then get and show user info from google account
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                //get user email and uid from auth
                                String email = firebaseAuth.getCurrentUser().getEmail();
                                String uid= firebaseAuth.getUid();
                                String name= firebaseAuth.getCurrentUser().getDisplayName();
                                //when user is registered to store data in firebase realtime database
                                //using hashmap

                                HashMap<Object, String> hashMap=new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name",name);
                                hashMap.put("Username","");
                                hashMap.put("phone","");
                                hashMap.put("profileImage","");
                                hashMap.put("coverImage","");
                                hashMap.put("onlineStatus","online");
                                hashMap.put("typingTo","noOne");
                                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                                //path to store user data name "Users"
                                DatabaseReference databaseReference=firebaseDatabase.getReference("Users");
                                //putting data within hashmap in database
                                databaseReference.child(uid).setValue(hashMap);
                            }
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i( "Fails", task.getException().getMessage());
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

