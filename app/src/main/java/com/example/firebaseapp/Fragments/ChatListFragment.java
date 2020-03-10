package com.example.firebaseapp.Fragments;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Adapter.AdapterAddPost;
import com.example.firebaseapp.Models.ModelPost;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterAddPost adapterAddPost;
    FloatingActionButton floatingActionButton;

    public ChatListFragment() {
        // Required empty public constructor
    }
}
