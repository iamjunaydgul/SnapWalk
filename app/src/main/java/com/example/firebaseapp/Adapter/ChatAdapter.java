package com.example.firebaseapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Models.ModelClassForChats;
import com.example.firebaseapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private static final int MSG_TYPE_RECEIVER=0; //left
    private static final int MSG_TYPE_SENDER=1;   //right

    private Context mContext;
    private List<ModelClassForChats> chatList;
    String leftImage,rightImage;

    /*Firebase*/
    FirebaseUser firebaseUser;


    public ChatAdapter(Context mContext, List<ModelClassForChats> chatList, String leftImage,String rightImage) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.leftImage = leftImage;
        this.rightImage = rightImage;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*inflate layout : rowChatReceiver and rowChatSender*/

        if(viewType==MSG_TYPE_SENDER){
            View rootView= LayoutInflater.from(mContext).inflate(R.layout.sender_chat_right,parent,false);
            return new MyViewHolder(rootView);
        }else{
            View rootView= LayoutInflater.from(mContext).inflate(R.layout.receiver_chat_left,parent,false);
            return new MyViewHolder(rootView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        final ModelClassForChats user= chatList.get(position);
        /*getData*/
        final String message=user.getMessage();
        String timeStamp= user.getTimeStamp();
        /*convert timeStamp to  dd/mm/yy hh:mm am/pm*/
        Calendar calender= Calendar.getInstance(Locale.ENGLISH);
        calender.setTimeInMillis(Long.parseLong(timeStamp));

        /*Important Note*/
        /*you have to call the the method through object. It is not a static method.*/
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String dateTime= df.format("dd/MM/yyy hh:mm aa",calender).toString();

        /*Set data*/
        holder.messageTextView.setText(message);
        holder.timeTextView.setText(dateTime);

        try{
            Picasso.get().load(leftImage).placeholder(R.drawable.ic_default_image).into(holder.leftUserImage);
        }catch (Exception e){
        }
        try{
            Picasso.get().load(rightImage).placeholder(R.drawable.ic_default_image).into(holder.rightUserImage);
        }catch (Exception e){
        }

        /*click to show DeleteDialg*/
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*show dlete message confirm dialg*/
                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                /*buttons*/
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        /*create and show dialog box*/
        /*Set Seen/deleivered*/
        if(position==chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.isSeentTextView.setText("Seen");
            }else{
                holder.isSeentTextView.setText("Delivered");
            }
        }else{
            holder.isSeentTextView.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }
    @Override
    public int getItemViewType(int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_SENDER;
        }else{
            return MSG_TYPE_RECEIVER;
        }
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView leftUserImage,rightUserImage;
        TextView messageTextView,timeTextView,isSeentTextView;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftUserImage =itemView.findViewById(R.id.receiverProfileImage);
            rightUserImage=itemView.findViewById(R.id.senderProfileImage);
            messageTextView=itemView.findViewById(R.id.receiverMessage);
            timeTextView=itemView.findViewById(R.id.receiverTimeTV);
            isSeentTextView=itemView.findViewById(R.id.isSeenTextView);
            linearLayout=itemView.findViewById(R.id.messageLayout);

        }
    }

    public void deleteMessage(int position){
        final String myUID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timeStamp=chatList.get(position).getTimeStamp();
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=databaseReference.orderByChild("timeStamp").equalTo(timeStamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    /*if you want to allow sender to delete only his messages then compare sender value with
                    * current signed in user's myUid if matches then delete*/
                    if(snapshot.child("Sender").getValue().equals(myUID)){
                        /*we can remove the message or we can replace it with This message was deleted...*/
                        snapshot.getRef().removeValue();

                        /*This message was deleted*/
                        /*HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        snapshot.getRef().updateChildren(hashMap);*/
                        Toast.makeText(mContext, "Message Deleted!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mContext, "You can delete only your messages!", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

