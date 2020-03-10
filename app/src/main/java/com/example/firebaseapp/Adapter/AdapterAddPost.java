package com.example.firebaseapp.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Activities.AddPostActivity;
import com.example.firebaseapp.Activities.CommentActivity;
import com.example.firebaseapp.Activities.ThereProfileActivity;
import com.example.firebaseapp.Models.ModelPost;
import com.example.firebaseapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterAddPost extends RecyclerView.Adapter<AdapterAddPost.MyHolder> {

    private Context context;
    private List<ModelPost> postList;

    /*delete post*/
    String myUid;
    /*post Likes*/
    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    boolean mProcessLike= false;


    public AdapterAddPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*inflate layout that we created row_user_recyclerView.xml*/
        View rootView= LayoutInflater.from(context).inflate(R.layout.row_home_recyclerview,parent,false);
        return new MyHolder(rootView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {


        /*getData*/
        final String userId = postList.get(position).getUid();
        String userEmail = postList.get(position).getEmail();
        String userName = postList.get(position).getuName();
        String userDp = postList.get(position).getuDp();
        final String postId = postList.get(position).getpId();
        String postTitle =postList.get(position).getpTitle();
        String postDescription = postList.get(position).getpDescription();
        final String postImage = postList.get(position).getpImage();
        String postTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); /*contains total number of likes*/
        String pComments = postList.get(position).getpComments(); /*contains total number of comments*/


        /*set likes for each post*/
        setLikes(holder,postId);


        /*convert TimeStamp to dd/mm/yy hh:mm am/pm*/
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(postTimeStamp));
        /*Important Note*/
        /*you have to call the the method through object. It is not a static method.*/
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String dateTime = df.format("dd/MM/yyy hh:mm aa", calendar).toString();

        /*setData*/
        holder.userNameTextView.setText(userName);
        holder.postTimeTextView.setText(dateTime);
        holder.postTitleTextView.setText(postTitle);
        holder.postDescriptionTextView.setText(postDescription);
        holder.postLikesTextView.setText(pLikes+" Likes");
        holder.postCommentsTextView.setText(pComments+" Comments");

        /*set User dP*/
        try {
            Picasso.get().load(userDp).placeholder(R.drawable.ic_default_image).into(holder.userPictureImageView);
        } catch (Exception e) {

        }
        /*set post Image
         * if there is on image i.e postImage.equals(noImage) then hide ImageView*/
        if(postImage.equals("noImage")){
            /*hide image view*/
            holder.postImageView.setVisibility(View.GONE);
        }else {
            /*show image view*//*
            holder.postImageView.setVisibility(View.VISIBLE);*/
            try {
                Picasso.get().load(postImage).into(holder.postImageView);
            } catch (Exception e) {

            }
        }


        /*handle button onClicks*/
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                /*delete Posts*/
                show_moreOptions(holder.moreButton, myUid,userId,postId,postImage);
            }
        });
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes= Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike=true;

                /*get id of the post clicked*/
                final String postId= postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mProcessLike){
                            if(dataSnapshot.child(postId).hasChild(myUid)){
                                /*already liked so remove like*/
                                postsRef.child(postId).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike=false;
                            }else{
                                /*not liked .... like it*/
                                postsRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postId).child(myUid).setValue("Liked");
                                mProcessLike= false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*start comment Activity*/
                /*go to theirProfileActivity with post id , this post id of clicked user which will be used to show user specific data/posts*/
                Intent intent= new Intent(context, CommentActivity.class);
                intent.putExtra("postId",postId);
                context.startActivity(intent);
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "shareButton", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*go to theirProfileActivity with myUid , this myUid of clicked user which will be used to show user specific data/posts*/
                Intent intent= new Intent(context, ThereProfileActivity.class);
                intent.putExtra("myUid",userId);
                context.startActivity(intent);
            }
        });


    }
    /*setLikes*/
    private void setLikes(final MyHolder holder, final String postKey) {

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild(myUid)){
                    /*user has liked this post
                     * to indicate that the post is liked by currently signed in user
                     * 1:change drawable left icon of like button
                     * 2:change text of like button from like to liked*/
                    holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_post_like,0,0,0);
                    holder.likeButton.setText("Liked");

                }else{
                    /*user has liked this post
                     * to indicate that the post is not liked by currently signed in user
                     * 1:change drawable left icon of like button
                     * 2:change text of like button from like to liked*/
                    holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.likeButton.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void show_moreOptions(ImageButton moreButton, String uid, String userId, final String postId, final String postImage) {

        /*Creating pop up menu currently having delete option,will add more options later*/
        final PopupMenu popupMenu= new PopupMenu(context,moreButton, Gravity.END);

        /*show delete option in only posts of currently signed in user*/
        if(uid.equals(userId)){
            /*add items in menu*/
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
            /*comment process*/
            popupMenu.getMenu().add(Menu.NONE,2,0,"Post Detail!");

        }

        /*item click listener*/
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==0){
                    /*delete is clicked*/
                    deletePost(postId,postImage);
                }
                if(id==1){
                    /*edit is clicked*/
                    /*start addpostActivity with the key 'editpost' and the if of the clicked post*/
                    Intent intent= new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    context.startActivity(intent);

                }if(id==2){
                    /*start comment Activity*/
                    /*go to theirProfileActivity with post id ,
                    this post id of clicked user which will be used to show user specific data/posts*/
                    Intent intent= new Intent(context, CommentActivity.class);
                    intent.putExtra("postId",postId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();

    }

    private void deletePost(String postId, String postImage) {

        /*post can be with or without image*/
        if(postImage.equals("noImage")){
            /*post is without image*/
            delete_withoutImage(postId);
        }else{
            /*post is with image*/
            delete_withImage(postId,postImage);
        }

    }

    private void delete_withoutImage(String postId) {
        /*progreass bar*/
        final ProgressDialog progressDialog= new ProgressDialog(context);
        /*image is deleted now delete from database*/
        Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue(); /*remove pId value from database if post id matched*/
                }
                Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void delete_withImage(final String postId, String postImage){
        /*progreass bar*/
        final ProgressDialog progressDialog= new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");
        /*Steps
        * 1:Delete image using url
        * 2:delete from database using post id*/
        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(postImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                /*image is deleted now delete from database*/
                Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            snapshot.getRef().removeValue(); /*remove pId value from database if post id matched*/
                        }
                        Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    /*View Holder Class*/
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView userPictureImageView,postImageView;
        TextView userNameTextView,postTimeTextView,postTitleTextView,postDescriptionTextView,postLikesTextView,postCommentsTextView;
        ImageButton moreButton;
        TextView likeButton,commentButton,shareButton;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            userPictureImageView= itemView.findViewById(R.id.uPictureIV);
            postImageView=itemView.findViewById(R.id.pImageView);

            /*Text and Image Views*/
            userNameTextView=itemView.findViewById(R.id.uNameTextView);
            postTimeTextView=itemView.findViewById(R.id.uTimeTV);
            postTitleTextView=itemView.findViewById(R.id.pTitleTV);
            postDescriptionTextView=itemView.findViewById(R.id.pDescriptionTV);
            postLikesTextView=itemView.findViewById(R.id.pLikesTV);
            postCommentsTextView=itemView.findViewById(R.id.pCommentsTv);


            /*moreButton*/
            moreButton=itemView.findViewById(R.id.moreButton);

            /*like,comment and share button*/
            likeButton=itemView.findViewById(R.id.likeButton);
            commentButton=itemView.findViewById(R.id.commentButton);
            shareButton=itemView.findViewById(R.id.shareButton);

            /*profile Layout*/
            profileLayout=itemView.findViewById(R.id.profileLayout);

        }
    }
}