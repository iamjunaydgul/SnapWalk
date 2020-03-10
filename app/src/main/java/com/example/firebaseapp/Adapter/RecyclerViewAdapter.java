package com.example.firebaseapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Activities.ChatActivity;
import com.example.firebaseapp.Activities.ThereProfileActivity;
import com.example.firebaseapp.Models.ModelClassforUsersRecyclerViewFragment;
import com.example.firebaseapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<ModelClassforUsersRecyclerViewFragment> mDataList;


    public RecyclerViewAdapter(Context mContext, List<ModelClassforUsersRecyclerViewFragment> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*inflate layout that we created row_user_recyclerView.xml*/
        View rootView= LayoutInflater.from(mContext).inflate(R.layout.row_users_recyclerview,parent,false);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final ModelClassforUsersRecyclerViewFragment user=mDataList.get(position);
        /*getData*/
        final String userUID=user.getUid();
        String userImage= user.getProfileImage();
        String userName= user.getName();
        final String userEmail= user.getEmail();

        /*Set data*/
        holder.userNameTextView.setText(userName);
        holder.userEmailTextView.setText(userEmail);

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.userCircularImageView);
        }catch (Exception e){

        }
        /*handle Item Clicks*/
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*show dialog...this is for showing the Their profile */
                AlertDialog.Builder builder= new AlertDialog.Builder(mContext);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            /*profile clicked*/
                            /*go to theirProfileActivity with myUid , this myUid of clicked user which will be used to show user specific data/posts*/
                            Intent intent= new Intent(mContext, ThereProfileActivity.class);
                            intent.putExtra("myUid",userUID);
                            mContext.startActivity(intent);
                        }
                        if(which==1){
                            /*chat clicked*/
                            /*Clicks user from user list to start chatting activity
                             * start activity by putting myUid of receiver
                             * we will use that myUid to identify the user we are gonna chat*/
                            Intent intent=new Intent(mContext, ChatActivity.class);
                            intent.putExtra("hisUid",userUID);
                            mContext.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });



    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView userCircularImageView;
        TextView userNameTextView,userEmailTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userCircularImageView=itemView.findViewById(R.id.userCircularImageView);
            userNameTextView=itemView.findViewById(R.id.userNameTextView);
            userEmailTextView=itemView.findViewById(R.id.userEmailTextView);

        }
    }

}

