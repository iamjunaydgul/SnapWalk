package com.example.firebaseapp.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.firebaseapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView registerButton,loginButton;
    Intent intent;
    LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        registerButton=findViewById(R.id.registerButton);
        loginButton=findViewById(R.id.loginButton);
        linearLayout=findViewById(R.id.circle);
        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        linearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerButton:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.loginButton:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.circle:
                startActivity(new Intent(this, RegisterActivity.class));
                break;


        }
    }
}
