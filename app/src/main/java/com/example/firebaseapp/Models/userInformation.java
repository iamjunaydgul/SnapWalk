package com.example.firebaseapp.Models;

public class userInformation {

    private static String userId,userEmail, userPassword;

    public userInformation(String email, String password) {
        this.userEmail = email;
        this.userPassword = password;
        this.userId= userId;
        //this.userName = name;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public userInformation(){

    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public String getUserPassword() {
        return userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}

