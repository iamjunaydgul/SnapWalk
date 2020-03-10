package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.firebaseapp.Activities.MainActivity;
import com.example.firebaseapp.Fragments.ChatListFragment;
import com.example.firebaseapp.Fragments.HomeFragment;
import com.example.firebaseapp.Fragments.ProfileFragment;
import com.example.firebaseapp.Fragments.UsersFragment;
import com.example.firebaseapp.R;
import com.example.firebaseapp.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity{
    Intent intent;
    FirebaseUser firebaseUser;
    ActionBar actionBar;
    BottomNavigationView bottomNavigationView;
    FragmentTransaction fragmentTransaction;

    /*NotificationsPart*/
    String mUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        actionBar = getSupportActionBar();
        //actionBar.setTitle(firebaseUser.getEmail()+" Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        /*IMPORTANT NOTE
        never ever setTitle of progressDialog in onCreate otherwise app crashes :)
        progressDialog.setTitle("Logging out...");*/
        init();
        checkUserStatus();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    public void init(){
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        //home Fragment default
        actionBar.hide();
        HomeFragment homeFragment= new HomeFragment();
        fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content,homeFragment,"");
        fragmentTransaction.commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //handle Item Clicks
                switch (menuItem.getItemId()){
                    //home Fragment Transaction
                    case R.id.nav_home:
                        actionBar.hide();
                        HomeFragment homeFragment= new HomeFragment();
                        fragmentTransaction=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content,homeFragment,"");
                        fragmentTransaction.commit();
                        return true;
                    //profile Fragment Transaction
                    case R.id.nav_profile:
                        actionBar.hide();
                        actionBar.setTitle("Profile");
                        ProfileFragment profileFragment= new ProfileFragment();
                        fragmentTransaction=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content,profileFragment,"");
                        fragmentTransaction.commit();
                        return true;
                    //users Fragment Transaction
                    case R.id.nav_users:
                        actionBar.hide();
                        actionBar.setTitle("Users");
                        UsersFragment usersFragment= new UsersFragment();
                        fragmentTransaction=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content,usersFragment,"");
                        fragmentTransaction.commit();
                        return true;
                    case R.id.nav_chat:
                        actionBar.hide();
                        actionBar.setTitle("Chats");
                        ChatListFragment chatListFragment= new ChatListFragment();
                        fragmentTransaction=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content,chatListFragment,"");
                        fragmentTransaction.commit();
                        return true;
                }
                return false;
            }
        });


    }

    private void updateToken(String token) {

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token mtoken=new Token(token);
        databaseReference.child(mUID).setValue(mtoken);

    }
    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            mUID=firebaseUser.getUid(); /*currently signed in user's ID*/

            /*this is done for notification part*/
            /*save currently signed in user UID in shared preferences*/
            SharedPreferences sharedPreferences=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

            /*update Token for Notification*/
            updateToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            intent=new Intent(DashboardActivity.this , MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }
}
