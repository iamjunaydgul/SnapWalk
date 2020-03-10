package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.Adapter.ChatAdapter;
import com.example.firebaseapp.Models.ModelClassForChats;
import com.example.firebaseapp.Models.ModelClassforUsersRecyclerViewFragment;
import com.example.firebaseapp.R;
import com.example.firebaseapp.notifications.APIService;
import com.example.firebaseapp.notifications.Client;
import com.example.firebaseapp.notifications.Data;
import com.example.firebaseapp.notifications.Response;
import com.example.firebaseapp.notifications.Sender;
import com.example.firebaseapp.notifications.Token;
import com.google.android.gms.common.api.Api;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    /*Views From XML*/
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView receiversProfileImageView;
    TextView receiversName,receiversStatus;
    EditText sendMessageEditText;
    ImageButton sendMessageButton;


    /*Strings*/
    String hisUID,myUID,hisImage,senderImage;

    /*Intent*/
    Intent intent;
    /*Firebase*/
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    /*for Checking if user has seen message or not*/
    ValueEventListener valueEventListener;
    List<ModelClassForChats> chatList;
    ChatAdapter chatAdapter;
    Query searchQuery;

    /*for Notification*/
    APIService apiService;
    boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        /*as we passwed userId from Users activity(RecyclerView Adapter onItemClickListener in OnBindViewHolder)
        * now getting it here so we can display receivers image and name on ChatActivity*/
        intent=getIntent();
        hisUID=intent.getStringExtra("hisUid");
        myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*Firebase Auth*/
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        /*forSetting leftAndRightImage in Chat (recievedMessages with sender Images)*/
        leftAndRightImage(hisUID,myUID);
        /*Layout for RecyclerView*/
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        /*check edit Text Change Listener*/
        sendMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(hisUID);/*uid of reciever*/
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        readMessages();
        seenMessage();
    }

    private void leftAndRightImage(String leftImage,String rightImage) {
        /*Search users against hisUID ,receiversID so that we can get his photo and name*/
        searchQuery=databaseReference.orderByChild("uid").equalTo(leftImage);
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    /*getData*/
                    String name = "" + snapshot.child("name").getValue();
                    hisImage = "" + snapshot.child("profileImage").getValue();
                    /*get Value of onlineStatus*/
                    String onlineStatus = "" + snapshot.child("onlineStatus").getValue();
                    /*typing or not*/
                    String typingto = "" + snapshot.child("typingTo").getValue();
                    /*check typing status*/
                    if (typingto.equals(myUID)) {
                        receiversStatus.setText("typing...");
                    } else {
                        /*setData on receivers image and name textview*/
                        receiversName.setText(name);
                        if (onlineStatus.equals("online")) {
                            receiversStatus.setText(onlineStatus);
                        } else {
                            Calendar calender = Calendar.getInstance(Locale.ENGLISH);
                            calender.setTimeInMillis(Long.parseLong(onlineStatus));

                            /*Important Note*/
                            /*you have to call the the method through object. It is not a static method.*/
                            android.text.format.DateFormat df = new android.text.format.DateFormat();
                            String dateTime = df.format("dd/MM/yyy hh:mm aa", calender).toString();
                            receiversStatus.setText("Last seen at:" + dateTime);
                        }

                    }
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_image).into(receiversProfileImageView);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_image).into(receiversProfileImageView);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        searchQuery=databaseReference.orderByChild("uid").equalTo(rightImage);
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    senderImage = "" + snapshot.child("profileImage").getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void seenMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        valueEventListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelClassForChats chat= snapshot.getValue(ModelClassForChats.class);
                    if(chat.getReciever().equals(myUID) && chat.getSender().equals(hisUID)){
                        HashMap<String,Object> hasSeenHashMap=new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        snapshot.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
        chatList =new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ModelClassForChats chat = snapshot.getValue(ModelClassForChats.class);
                    if(chat.getReciever().equals(myUID) && chat.getSender().equals(hisUID) ||
                    chat.getReciever().equals(hisUID) && chat.getSender().equals(myUID)){

                        chatList.add(chat);

                    }else{
                        Toast.makeText(ChatActivity.this, "Not Okay", Toast.LENGTH_SHORT).show();
                    }
                    chatAdapter =new ChatAdapter(ChatActivity.this,chatList,hisImage,senderImage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void init(){
        /*never forget if you want action bar replaces with you custom toolbar*/
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        recyclerView=findViewById(R.id.chat_RecyclerView);
        receiversProfileImageView=findViewById(R.id.receiversProfilePictureIV);
        receiversName=findViewById(R.id.receiversNameTV);
        receiversStatus=findViewById(R.id.receiversStatus);
        sendMessageEditText=findViewById(R.id.messageEditText);
        sendMessageButton=findViewById(R.id.sendMessageButton);

        /*Creating API Service*/
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

    }
    /*wether user logged in or not*/
    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            myUID=firebaseUser.getUid(); /*currently signed in user's ID*/
        }else{
            intent=new Intent(ChatActivity.this , MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        /*hide Search View/add post as we don't need it here*/
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

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

    /*Sending the Message*/
    public void sendMessage(View view){
        /*for Notification*/
        notify=true;
        final String message=sendMessageEditText.getText().toString().trim();
        /*check if empty or not*/
        if(!message.isEmpty()){
            String timeStamp= String.valueOf(System.currentTimeMillis());
            /*getting the top node reference that is above Users*/
            databaseReference=FirebaseDatabase.getInstance().getReference();
            /*making new child of top node named Chats containing childs like senderID,ReceiverId and the Message*/
            HashMap<String,Object> messageInfo=new HashMap<>();
            messageInfo.put("Sender",myUID);
            messageInfo.put("Reciever",hisUID);
            messageInfo.put("message",message);
            messageInfo.put("timeStamp",timeStamp);
            messageInfo.put("isSeen",false);
            databaseReference.child("Chats").push().setValue(messageInfo);

            /*for Notification*/
            final DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference("Users").child(myUID);
            databaseReference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ModelClassforUsersRecyclerViewFragment user=dataSnapshot.getValue(ModelClassforUsersRecyclerViewFragment.class);
                    if(notify){
                        sendNotification(hisUID,user.getName(),message);
                    }
                    notify=false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else{
            Toast.makeText(this, "Please write something to send...", Toast.LENGTH_SHORT).show();
        }
        /*reset EditText after sending message*/
        sendMessageEditText.setText("");
    }

    /*this method is called when user send message*/
    private void sendNotification(final String hisUID, final String name, final String message) {

        DatabaseReference allTokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Token token =snapshot.getValue(Token.class);
                    Data data=new Data(myUID,name+":"+message,"New Message",hisUID,R.drawable.ic_default_image);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*set last seen with time Stamp*/
        String timeStamp= String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    /*checking wether user online of ofline*/
    public void checkOnlineStatus(String status){

        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String,Object>  hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        databaseReference.updateChildren(hashMap);

    }
    /*checking wether user online and typing*/
    public void checkTypingStatus(String typing){
        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String,Object>  hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        databaseReference.updateChildren(hashMap);
    }
}
