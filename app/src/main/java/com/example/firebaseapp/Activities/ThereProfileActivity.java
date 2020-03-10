package com.example.firebaseapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Adapter.AdapterAddPost;
import com.example.firebaseapp.Models.ModelPost;
import com.example.firebaseapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThereProfileActivity extends AppCompatActivity {
    /*see my posts and others profile*/
    RecyclerView postsRecyclerView;
    /*List for see my and others profile */
    List<ModelPost> postList;
    AdapterAddPost adapterAddPost;
    String uid;

    /*Firebase*/
    FirebaseAuth firebaseAuth;

    /*Views From XML*/
    ImageView profileImageView,coverImageView;
    TextView nameTV, emailTV, phoneTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        /*initialize views from xml*/
        postsRecyclerView = findViewById(R.id.see_myPosts_recyclerView);
        /*initialize XML views*/
        profileImageView = findViewById(R.id.avatarImageView);
        coverImageView = findViewById(R.id.coverImageView);
        nameTV = findViewById(R.id.nameTextView);
        emailTV = findViewById(R.id.emailTextView);
        phoneTV = findViewById(R.id.phoneTextView);
        firebaseAuth = FirebaseAuth.getInstance();

        /*getting uid of clicked user to retrieve his posts*/
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GetnSetUserDetail(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        postList = new ArrayList<>();

        /*load the posts of clicked user*/
        load_clickedUserPost();
        checkUserStatus();

    }

    private void load_clickedUserPost() {

        /*Linearlayout for recyclerView(for current user posts)*/
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        /*show newest post first for this load from last*/
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        /*setThis layout to recycler view of profileFragment*/
        postsRecyclerView.setLayoutManager(layoutManager);

        /*init post list*/
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        /*query*/
        Query query= ref.orderByChild("uid").equalTo(uid);

        /*get All data of the matched query*/
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPost myPost= snapshot.getValue(ModelPost.class);
                    /*add to list*/
                    postList.add(myPost);

                    /*adapter setting */
                    adapterAddPost= new AdapterAddPost(ThereProfileActivity.this,postList);
                    postsRecyclerView.setAdapter(adapterAddPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void search_clickedUserPost(final String searchQuery){

        /*Linearlayout for recyclerView(for current user posts)*/
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        /*show newest post first for this load from last*/
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        /*setThis layout to recycler view of profileFragment*/
        postsRecyclerView.setLayoutManager(layoutManager);

        /*init post list*/
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        /*query*/
        final Query query= ref.orderByChild("uid").equalTo(uid);

        /*get All data of the matched query*/
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPost modelPost= snapshot.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    /*add to list*/
                    postList.add(modelPost);

                    /*adapter setting */
                    adapterAddPost= new AdapterAddPost(ThereProfileActivity.this,postList);
                    postsRecyclerView.setAdapter(adapterAddPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        /*hide addPost*/
        menu.findItem(R.id.action_add_post).setVisible(false);

        /*SearchView*/
        MenuItem item= menu.findItem(R.id.action_search);
        final SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        /*Search listener*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*called when user Press search button from keyboard*/
                if(!query.trim().isEmpty()){
                    search_clickedUserPost(query);
                }else{
                    load_clickedUserPost();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                /*called whenever user press any single letter*/
                if(!query.trim().isEmpty()){
                    search_clickedUserPost(query);
                }else{
                    load_clickedUserPost();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    //handle menu iteme clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id= item.getItemId();
        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            /*user is signed in stay here
             * set Email for logged in user
             * mprofile.setTExt(user.getEmail)*/

        }else{
            intent=new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    public void GetnSetUserDetail(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Map<Object, String> data = (Map<Object, String>) snapshot.getValue();
            /*Getting data from FirebaseDatabase*/
            String name, phone, email, profileImage, coverImage;
            name = data.get("name");
            phone = data.get("phone");
            email = data.get("email");
            profileImage = data.get("profileImage");
            coverImage = data.get("coverImage");

            /*Setting data on XML TextViews*/
            nameTV.setText(name);
            emailTV.setText(email);
            phoneTV.setText(phone);

            /*for Loading Image on XML ImageView
             * We are using Picasso Library that gets the job done for us*/
            try {
                /* If success then show image on profileImageView*/
                Picasso.get().load(profileImage).into(profileImageView);

            } catch (Exception e) {
                /*if Fails then load default Image on both XML ImageView*/
                Picasso.get().load(R.drawable.ic__add_image).into(profileImageView);
            }
            try {
                Picasso.get().load(coverImage).into(coverImageView);
            }catch (Exception e){
                Picasso.get().load(R.drawable.ic__add_image).into(coverImageView);
            }
        }
    }



}
