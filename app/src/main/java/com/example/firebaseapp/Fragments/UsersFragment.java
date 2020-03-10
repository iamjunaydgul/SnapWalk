package com.example.firebaseapp.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.example.firebaseapp.Activities.MainActivity;
import com.example.firebaseapp.Models.ModelClassforUsersRecyclerViewFragment;
import com.example.firebaseapp.R;
import com.example.firebaseapp.Adapter.RecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    List<ModelClassforUsersRecyclerViewFragment> userList;
    /*Firebase*/
    private DatabaseReference databaseReference;

    public UsersFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView=view.findViewById(R.id.usersRecyclerView);
        //set recyclerView Properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /*RecyclerView and Adapter*/
        userList =new ArrayList<>();
        getAllUsers();
        return view;
    }

    private void getAllUsers(){
        /*Firebase*/
        /*getting path of the database*/
        /*getting current signed in user*/
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelClassforUsersRecyclerViewFragment modelUser=snapshot.getValue(ModelClassforUsersRecyclerViewFragment.class);
                    if(!modelUser.getUid().equals(firebaseUser.getUid())){
                        userList.add(modelUser);
                    }
                    recyclerViewAdapter =new RecyclerViewAdapter(getActivity(),userList);
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(recyclerViewAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);/*to show menu options in fragments*/
        /*DatabaseRefernece*/
        super.onCreate(savedInstanceState);
    }

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /*inflate menu*/
        inflater.inflate(R.menu.menu_main,menu);
        /*hide addPost icon from usersFragment*/
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
                    searchUser(query);
                }else{
                    getAllUsers();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                /*called whenever user press any single letter*/
                if(!newText.trim().isEmpty()){
                    searchUser(newText);
                }else{
                    getAllUsers();
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
        return super.onOptionsItemSelected(item);
    }

    /*wether user logged in or not*/
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
    public void searchUser(final String query){
        /*Firebase*/
        /*getting path of the database*/
        /*getting current signed in user*/
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelClassforUsersRecyclerViewFragment m1=snapshot.getValue(ModelClassforUsersRecyclerViewFragment.class);
                    /*Condition to fulfil search
                     * 1:not searched current user
                     * 2:user name or email contains text entered in searchView(Case Sensitive)*/
                    /*get all searched users except currently signed in user*/
                    if(!m1.getUid().equals(firebaseUser.getUid())){
                        if(m1.getName().toLowerCase().contains(query.toLowerCase()) ||
                        m1.getEmail().toLowerCase().contains(query.toLowerCase())){
                            Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                            userList.add(m1);
                        }
                    }
                    recyclerViewAdapter=new RecyclerViewAdapter(getActivity(), userList);
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(recyclerViewAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
