package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashMap;
import java.util.Locale;

public class CommentActivity extends AppCompatActivity {
    /*Views from xml*/
    ImageView uPictureIv,pImageIv;
    TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    /*wasted 1 hour,issue is app crashing because like and share are not buttons they are textViews shit...*/
    TextView likeButton,shareButton;
    LinearLayout profileLayout;

    /*Comment Views from xml*/
    EditText commentEt;
    ImageButton sendCommentButton;
    ImageView comment_uProfileIv;

    /*Comment Process*/
    /*to get detail of clicked post and user of post*/
    String myUid,myEmail,myName,myDp,postId,pLikes,hisDp,hisName;
    boolean mProcessComments=false;
    boolean mProcessLike= false;
    String hisUid,pImage;


    /*progress Dialog*/
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        /*initialize everything in this method*/
        init();
        checkUserStatus();
        /*getting information of post that is clicked*/
        load_postInfo();
        /*getting information of the user that posted the post*/
        load_userInfo();
        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
       /* post like button clicked in CommentActivity*/
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
        /*more button click handles*/
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                show_moreOptions();
            }
        });
    }
    /*init Views*/
    private void init(){

        /*Action Bar and its properties*/
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        uPictureIv= findViewById(R.id.uPictureIV);
        pImageIv=findViewById(R.id.pImageView);

        /*Text and Image Views*/
        uNameTv =findViewById(R.id.uNameTextView);
        pTimeTv=findViewById(R.id.uTimeTV);
        pTitleTv=findViewById(R.id.pTitleTV);
        pDescriptionTv=findViewById(R.id.pDescriptionTV);
        pLikesTv=findViewById(R.id.pLikesTV);
        pCommentsTv=findViewById(R.id.pCommentsTv);

        /*moreButton*/
        moreBtn=findViewById(R.id.moreButton);

        /*like,comment and share button*/
        likeButton=findViewById(R.id.likeBt_commentActivity);
        shareButton=findViewById(R.id.shareBt_commentActivity);
        profileLayout=findViewById(R.id.profileLayout);

        /*commentSection*/
        commentEt=findViewById(R.id.commentEditText);
        sendCommentButton=findViewById(R.id.commentSendButton);
        comment_uProfileIv=findViewById(R.id.comment_imageView);

        /*progressDialog*/
        progressDialog= new ProgressDialog(this);
        /*getting post id that is sent from AdapterAddPost Activity from OnBindView method*/
        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");

        /*set likes for each post*/
        setLikes();

    }
    /*wether user logged in or not*/
    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            myEmail=firebaseUser.getEmail(); /*currently signed in user's ID*/
            myUid= firebaseUser.getUid();
        }else{
            intent=new Intent(CommentActivity.this , MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    /*load the post that is clicked*/
    private void load_postInfo() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
        /*get Detail of post using id of post
         * match post id with the id that is sent from AdapterAddPost Activity through intent*/
        Query query=reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    /*getData*/
                    String pTitle= ""+snapshot.child("pTitle").getValue();
                    String pDescription= ""+snapshot.child("pDescription").getValue();
                    String pTimeStamp= ""+snapshot.child("pTime").getValue();
                    pLikes= ""+snapshot.child("pLikes").getValue();
                    pImage= ""+snapshot.child("pImage").getValue();
                    hisDp= ""+snapshot.child("uDp").getValue();
                    hisUid= ""+snapshot.child("uid").getValue();
                    String uEmail= ""+snapshot.child("uEmail").getValue();
                    hisName= ""+snapshot.child("uName").getValue();
                    String commentCount= ""+snapshot.child("pComments").getValue();

                    /*convert TimeStamp to properFormat*/
                    /*convert TimeStamp to dd/mm/yy hh:mm am/pm*/
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    /*Important Note*/
                    /*you have to call the the method through object. It is not a static method.*/
                    android.text.format.DateFormat df = new android.text.format.DateFormat();
                    String dateTime = df.format("dd/MM/yyy hh:mm aa", calendar).toString();


                    /*now setData on xml Views*/
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescription);
                    pLikesTv.setText(pLikes+" Likes");
                    pTimeTv.setText(dateTime);
                    uNameTv.setText(hisName);
                    pCommentsTv.setText(commentCount+" Comments");

                    /*set post Image
                     * if there is on image i.e postImage.equals(noImage) then hide ImageView*/
                    if(pImage.equals("noImage")){
                        /*hide image view*/
                        pImageIv.setVisibility(View.GONE);
                    }else {
                        /*show image view*/
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }

                    /*set dp of the currently signed in user who is going to comment on the post in comment section*/
                    /*set User dP*/
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_image).into(uPictureIv );
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_image).into(uPictureIv );

                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    /*load the info of the user that posted the post*/
    private void load_userInfo() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        Query query=reference.orderByChild("uid").equalTo(myUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {

                    /*getData*/
                    myName= ""+snapshot.child("name").getValue();
                    myDp= ""+snapshot.child("profileImage").getValue();

                    /*setData*/
                    /*set User dP*/
                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_image).into(comment_uProfileIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_image).into(comment_uProfileIv );
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    /*uploadComment by clicking on send comment button*/
    private void postComment() {

        progressDialog.setMessage("Adding comment...");
        progressDialog.show();
        /*getData from comment edit Text*/
        final String comment= commentEt.getText().toString();
        /*check empty or not*/
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "cannot send empty comment...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }else{
            String timeStamp= String.valueOf(System.currentTimeMillis());
            /*each post have a child comment containing comments of that posts*/
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

            HashMap<String,Object> hashMap= new HashMap<>();
            /*put data in comment node*/
            hashMap.put("cId",timeStamp);
            hashMap.put("comment",comment);
            hashMap.put("timeStamp",timeStamp);
            hashMap.put("uid",myUid);
            hashMap.put("uEmail",myEmail);
            hashMap.put("uDp",myDp);
            hashMap.put("uName",myName);

            /*put data in database reference*/
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    /*comment added*/
                    progressDialog.dismiss();
                    Toast.makeText(CommentActivity.this, "Comment posted...", Toast.LENGTH_SHORT).show();
                    commentEt.setText("");
                    /*this method is for counting how many comments on the post like we did for Likes*/
                    updateCommentCount();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    /*comment not added*/
                    progressDialog.dismiss();
                    Toast.makeText(CommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }
    /*update comment count just we did for like*/
    private void updateCommentCount() {
        /*this method is for counting how many comments on the post like we did for Likes*/
        mProcessComments=true;
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessComments){
                    if(dataSnapshot.child(postId).hasChild(myUid)){
                        String comments= ""+ dataSnapshot.child("pComments").getValue();
                        int newCommentVal= Integer.parseInt(comments)+1;
                        databaseReference.child("pComments").setValue(""+newCommentVal);
                        mProcessComments=false;

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    /*like post in comment Activity*/
    private void likePost() {

        mProcessLike=true;
        final DatabaseReference likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef= FirebaseDatabase.getInstance().getReference().child("Posts");

        /*get id of the post clicked*/
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessLike){
                    if(dataSnapshot.child(postId).hasChild(myUid)){
                        /*already liked so remove like*/
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1)) ;
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike=false;

                    }else{
                        /*not liked .... like it*/
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    /*when user click on more button of CommentActivity then show delete,edit options*/
    private void show_moreOptions() {

        /*Creating pop up menu currently having delete option,will add more options later*/
        final PopupMenu popupMenu= new PopupMenu(this,moreBtn, Gravity.END);

        /*show delete option in only posts of currently signed in user*/
        if(hisUid.equals(myUid)){
            /*add items in menu*/
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }

        /*item click listener*/
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==0){
                    /*delete is clicked*/
                    deletePost();
                }if(id==1){
                    /*edit is clicked*/
                    /*start addpostActivity with the key 'editpost' and the if of the clicked post*/
                    Intent intent= new Intent(CommentActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();

    }
    private void deletePost() {

        /*post can be with or without image*/
        if(pImage.equals("noImage")){
            /*post is without image*/
            delete_withoutImage();
        }else{
            /*post is with image*/
            delete_withImage();
        }
    }
    private void delete_withImage() {
        /*progreass bar*/
        final ProgressDialog progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        /*Steps
         * 1:Delete image using url
         * 2:delete from database using post id*/
        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
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
                        Toast.makeText(CommentActivity.this, "Post Deleted!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void delete_withoutImage() {

        /*progreass bar*/
        final ProgressDialog progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        /*image is deleted now delete from database*/
        Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue(); /*remove pId value from database if post id matched*/
                }
                Toast.makeText(CommentActivity.this, "Post Deleted!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void setLikes() {
        /*when detail of the post is loading...also check if current user has liked or or not*/
        final DatabaseReference likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUid)){
                    /*user has liked this post
                     * to indicate that the post is liked by currently signed in user
                     * 1:change drawable left icon of like button
                     * 2:change text of like button from like to liked*/
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_post_like,0,0,0);
                    likeButton.setText("Liked");

                }else{
                    /*user has liked this post
                     * to indicate that the post is not liked by currently signed in user
                     * 1:change drawable left icon of like button
                     * 2:change text of like button from like to liked*/
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likeButton.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
