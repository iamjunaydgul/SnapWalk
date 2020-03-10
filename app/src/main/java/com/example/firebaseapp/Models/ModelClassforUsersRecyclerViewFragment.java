package com.example.firebaseapp.Models;

public class ModelClassforUsersRecyclerViewFragment {
    /*using same name as in Firebase database*/
    String name,phone,email,profileImage,coverImage,search,uid;

    public ModelClassforUsersRecyclerViewFragment(String name, String phone, String email, String profileImage, String coverImage, String search, String uid) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.profileImage = profileImage;
        this.coverImage = coverImage;
        this.search = search;
        this.uid = uid;
    }

    /*never ever forget to write blank constructor for the class otherwise waste a lot of time just like me :)
    * seriously wasted a lot of time may be 2 hours only for this shit haha*/
    public ModelClassforUsersRecyclerViewFragment(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
