package com.example.firebaseapp.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebaseapp.Activities.AddPostActivity;
import com.example.firebaseapp.Activities.MainActivity;
import com.example.firebaseapp.Adapter.AdapterAddPost;
import com.example.firebaseapp.Adapter.RecyclerViewAdapter;
import com.example.firebaseapp.Models.ModelClassforUsersRecyclerViewFragment;
import com.example.firebaseapp.Models.ModelPost;
import com.example.firebaseapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterAddPost adapterAddPost;
    FloatingActionButton floatingActionButton;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);
        /*Recycler View*/
        recyclerView=view.findViewById(R.id.postRecyclerView);
        floatingActionButton = view.findViewById(R.id.floatingActionHomeButton);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        /*show newest post first , load from last */
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        /*setLayout to recyclerView*/
        recyclerView.setLayoutManager(layoutManager);
        /*init postList*/
        postList=new ArrayList<>();
        loadPosts();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),AddPostActivity.class));
            }
        });
        return view;
    }

    private void loadPosts() {
        /*path of all posts*/
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        /*get all data from this posts reference*/
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    ModelPost modelPost=snapshot.getValue(ModelPost.class);
                    postList.add(modelPost);
                    adapterAddPost=new AdapterAddPost(getActivity(),postList);
                    adapterAddPost.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterAddPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);/*to show menu options in fragments*/
        super.onCreate(savedInstanceState);
    }
    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);


        /*SearchView*/
        MenuItem item= menu.findItem(R.id.action_search);
        final SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        /*Search listener*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*called when user Press search button from keyboard*/
                if(!query.trim().isEmpty()){
                    searchPost(query);
                }else{
                    loadPosts();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                /*called whenever user press any single letter*/
                if(!newText.trim().isEmpty()){
                    searchPost(newText);
                }else{
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
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
        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
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
            intent=new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }
    /*searchPost*/
    public void searchPost(final String query){

        /*path of all posts*/
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        /*get all data from this posts reference*/
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    ModelPost modelPost=snapshot.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(query.toLowerCase())||
                            modelPost.getpDescription().toLowerCase().contains(query.toLowerCase())){
                        postList.add(modelPost);
                    }
                    adapterAddPost=new AdapterAddPost(getActivity(),postList);
                    adapterAddPost.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterAddPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}