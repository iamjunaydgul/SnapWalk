package com.example.firebaseapp.Models;

public class ModelClassForChats {
    String message, Reciever, Sender,timeStamp;
    boolean isSeen;

    public ModelClassForChats(){
    }
    public ModelClassForChats(String message, String reciever, String sender, String timeStamp, boolean isSeen) {
        this.message = message;
        Reciever = reciever;
        Sender = sender;
        this.timeStamp = timeStamp;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReciever() {
        return Reciever;
    }

    public void setReciever(String reciever) {
        Reciever = reciever;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
