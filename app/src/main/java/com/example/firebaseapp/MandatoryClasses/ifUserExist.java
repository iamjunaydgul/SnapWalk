package com.example.firebaseapp.MandatoryClasses;

import android.app.Application;
import android.content.Intent;

import com.example.firebaseapp.Activities.DashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ifUserExist extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null && firebaseUser.isEmailVerified()){
            startActivity(new Intent(this, DashboardActivity.class));
        }
    }
}
