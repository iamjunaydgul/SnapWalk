package com.example.firebaseapp.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.Activities.AddPostActivity;
import com.example.firebaseapp.Activities.MainActivity;
import com.example.firebaseapp.Activities.ThereProfileActivity;
import com.example.firebaseapp.Adapter.AdapterAddPost;
import com.example.firebaseapp.Models.ModelPost;
import com.example.firebaseapp.R;
import com.example.firebaseapp.Models.userInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    /*Firebase*/
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    /*path where users Image and Cover will be uploaded*/
    String storagePath="Users_Profile_Cover_Images/";

    /*Views From XML*/
    ImageView profileImageView,coverImageView;
    TextView nameTV, emailTV, phoneTV;
    FloatingActionButton floatingActionButton;

    /*See my and others Profile Activity*/
    /*see my posts and others profile*/
    RecyclerView postsRecyclerView;
    /*List for see my and others profile */
    List<ModelPost> postList;
    AdapterAddPost adapterAddPost;
    String uid;


    /*userInformation Class Object*/
    com.example.firebaseapp.Models.userInformation userInformation = new userInformation();

    /*progressDialog*/
    ProgressDialog progressDialog;

    /*Mandatory*/

    /*if profilePicture then profileOrCoverPhoto=profileImage
    * if coverPicture then profileOrCoverPhoto = coverImage*/
    String profileOrCoverPhoto;

    /*Permissions Constants*/
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int  STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    /*Arrays of Permission to be requested*/
    String cameraPermissions[];
    String storagePermissions[];



    /*Image URI*/
    Uri image_uri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*Inflate the layout for this fragment*/
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        /*Firebase instances & references*/
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        /*initialize XML views*/
        profileImageView = view.findViewById(R.id.avatarImageView);
        coverImageView = view.findViewById(R.id.coverImageView);
        nameTV = view.findViewById(R.id.nameTextView);
        emailTV = view.findViewById(R.id.emailTextView);
        phoneTV = view.findViewById(R.id.phoneTextView);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        postsRecyclerView = view.findViewById(R.id.see_myPosts_recyclerView);


        /*initialize Progress Dialog*/
        progressDialog = new ProgressDialog(getActivity());

        /*We have to get currentlySignedIn user details by email or UID
         * We are getting details by userEmail
         * By using orderByChild query or by addSingleValueEventListener to get Details
         * Here we are using query*/
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GetnSetUserDetail(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*Handling FloatingActionButton*/
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        /*initializing permissions of Arrays*/
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        /*checkUserStatus and loadmyPosts*/
        checkUserStatus();
        load_myPosts();
        postList = new ArrayList<>();


        return view;
    }

    /*load current user all posts*/
    private void load_myPosts() {

        /*Linearlayout for recyclerView(for current user posts)*/
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        /*show newest post first for this load from last*/
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        /*setThis layout to recycler view of profileFragment*/
        postsRecyclerView.setLayoutManager(layoutManager);

        /*init post list*/
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        /*query*/
        Query query= ref.orderByChild("uid").equalTo(uid);

        /*get All data of the matched query*/
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    /*clear the list other wise you see duplicates posts*/
                    postList.clear();
                    ModelPost myPost= snapshot.getValue(ModelPost.class);
                    /*add to list*/
                    postList.add(myPost);

                    /*adapter setting */
                    adapterAddPost= new AdapterAddPost(getActivity(),postList);
                    adapterAddPost.notifyDataSetChanged();
                    postsRecyclerView.setAdapter(adapterAddPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
    /*Functions/Methods*/

    public void GetnSetUserDetail(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Map<Object, String> data = (Map<Object, String>) snapshot.getValue();
            /*Getting data from FirebaseDatabase*/
            String name, phone, email, profileImage, coverImage;
            name = data.get("name");
            phone = data.get("phone");
            email = data.get("email");
            profileImage = data.get("profileImage");
            coverImage = data.get("coverImage");

            /*Setting data on XML TextViews*/
            nameTV.setText(name);
            emailTV.setText(email);
            phoneTV.setText(phone);

            /*for Loading Image on XML ImageView
            * We are using Picasso Library that gets the job done for us*/
            try {
                /* If success then show image on profileImageView*/
                Picasso.get().load(profileImage).into(profileImageView);

            } catch (Exception e) {
               /*if Fails then load default Image on both XML ImageView*/
                    Picasso.get().load(R.drawable.ic__add_image).into(profileImageView);
            }
            try {
                Picasso.get().load(coverImage).into(coverImageView);
            }catch (Exception e){
                Picasso.get().load(R.drawable.ic__add_image).into(coverImageView);
            }
        }
    }
    public void showEditProfileDialog() {
       /* Showing Dialog box containing Edit profilePhoto,name,coverPhoto,phone*/
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};
        /*AlertDialog*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items clicks
                if (which == 0) {
                    /*Edit profilePhoto clicked*/
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "profileImage";
                    showImagePicDialog();
                }
                if (which == 1) {
                    /*Edit coverPhoto clicked*/
                    progressDialog.setMessage("Updating Cover Picture");
                    profileOrCoverPhoto = "coverImage";
                    showImagePicDialog();
                }
                if (which == 2) {
                    /*Edit name clicked*/
                    progressDialog.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                if (which == 3) {
                    /*Edit profilePhoto clicked*/
                    progressDialog.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();
    }
    public void showImagePicDialog() {
        /*Showing Dialog Containing Camera & Gallery
        * Gallery and Camera Image take Permission Required*/
        String options[] = {"Camera", "Gallery"};
        /*Alert Dialog*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*handles onClick
                * if Camera then go to Mobile Camera if have permission
                * else go to mobile gallery to pick one*/
                if (which == 0) {
                    /*Camera Button Clicked*/
                    if(!checkCamerPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                }
                if (which == 1) {
                    /*GalleryButton Clicked*/
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    /*Camera and Storage/Gallery Permissions*/
    private boolean checkStoragePermission() {
        /*Checking if Storage Permission is Enabled or Not
        return true if Enabled otherwise return false*/
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        /*Requesting for Storage Permission on Runtime*/
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }
    private boolean checkCamerPermission() {
        /*Checking if Camera Permission is Enabled or Not
        return true if Enabled otherwise return false*/
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission() {
        /*Requesting for Camera Permission on Runtime*/
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    /*Permission allow or denied from DialogBox*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method is called when user allow or deny from permission request dialog
        Here we will handle permission cases :)*/
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                //picking from camera ,first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        /*permissions allowed you can take image from camera */
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Please enabled storage and camera permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                //picking from gallery ,first check if storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        /*permissions allowed you can take image from gallery */
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please enabled storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    /*Take pic from Camera*/
    private void pickFromCamera() {
        /*Intent of picking image from device camera*/
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        /*Put image URI*/
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        /*Intent To start CameraActivity*/
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }
    /*Pick pic from Gallery*/
    private void pickFromGallery() {
        /*Pick from Gallery*/
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    /*Understand Cycle of This Permissions
    * 1st:if there is no permission,request for permission
    * 2nd:if request for permission then go to onRequestPermissionResult Function where it direct user where he desire like in our
    * case if user request for camera then onRequestPermissionResult Function will take user to Mobile Camera
    * 3rd:after taking user to Mobile Camera next step is what after getting the camera e.g if user take pic from camera
    * then onActivityResult Function handles what to do with the picture*/

    /*This Method is called after picking image from gallery or from camera*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE){
                //image is picked from gallery get uri of image
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
                //Toast.makeText(getActivity(), "IMAGE_PICK_GALLERY_REQUEST_CODE", Toast.LENGTH_LONG).show();
            }
            if(requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                //image is picked from camera get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /*upload Profile or Cover Photo*/
    private void uploadProfileCoverPhoto(Uri uri) {
        progressDialog.show();
        /* instead of creating different functions for profile and cover photos,doing work for both in same function
        * by just creating a String profileOrCoverPhoto
        * if profilePicture then profileOrCoverPhoto=profileImage
         * if coverPicture then profileOrCoverPhoto = coverImage*/

        String filePathAndName= storagePath+""+profileOrCoverPhoto+"_"+firebaseUser.getUid();
        StorageReference storageReference2nd=storageReference.child(filePathAndName);

        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //images is uploaded to storage ,now get its uri and store in user's database;
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                final Uri downloadUri=uriTask.getResult();
                //check if image is uploaded or not and uri recieved
                if(uriTask.isSuccessful()){
                    /*image uploaded
                     *add or update uri in user's database*/
                    HashMap<String,Object> result=new HashMap<>();
                    result.put(profileOrCoverPhoto,downloadUri.toString());
                    databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //URI added in database ,dismiss progress bar
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Image Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error adding uri dismiss progress bar
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Error Occurred...", Toast.LENGTH_SHORT).show();
                        }
                    });

                    /*see my posts and other users profiles*/
                    /*if user edit his also change his name on posts*/
                    if(profileOrCoverPhoto.equals("image")){
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                        Query query= ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    String child=snapshot.getKey();
                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        /*for Comments*/
                        /*update user image in CUrrent users comments of posts*/
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String child = snapshot.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")) {
                                        String child1 = dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    String child = snapshot.getKey();
                                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Some Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //images is not uploaded to storage ,get and show error
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*update phone and name*/
    public void showNamePhoneUpdateDialog(final String key){
       /* paramater key contain value either name or phone
         * name is key in user database to update user name
         * phone to update user phone*/
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        //set layout of dialoge
        LinearLayout linearLayout =new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit Text
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+ key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        //add buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.setTitle("Updating "+key);
                progressDialog.show();
                final String value=editText.getText().toString();
                if(!value.isEmpty()){
                    HashMap<String,Object> result=new HashMap<>();
                    result.put("/"+firebaseUser.getUid()+"/"+key,value);
                    databaseReference.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),key+" Updated!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    /*see my posts and other users profiles*/
                    /*if user edit his also change his name on posts*/
                    if(key.equals("name")){
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                        Query query= ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    String child=snapshot.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        /*for Comments*/
                        /*update name  in current users comments on posts*/
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    String child=snapshot.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")){
                                        String child1= dataSnapshot.child(child).getKey();
                                        Query child2= FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                                    String child=snapshot.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }else{
                    Toast.makeText(getActivity(),"Please enter "+key, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }
    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);/*to show menu options in fragments*/
        super.onCreate(savedInstanceState);
    }
    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        /*SearchView*/
        MenuItem item= menu.findItem(R.id.action_search);
        final SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        /*Search listener*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*called when user Press search button from keyboard*/
                if(!query.trim().isEmpty()){
                    search_myPosts(query);
                }else{
                    load_myPosts();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                /*called whenever user press any single letter*/
                if(!newText.trim().isEmpty()){
                    search_myPosts(newText);
                }else{
                    load_myPosts();
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
        }if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            /*user is signed in stay here
             * set Email for logged in user
             * mprofile.setTExt(user.getEmail)*/

            uid=firebaseUser.getUid();

        }else{
            intent=new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }

    /*load current user all posts*/
    private void search_myPosts(final String searchQuery) {

        /*Linearlayout for recyclerView(for current user posts)*/
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        /*show newest post first for this load from last*/
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        /*setThis layout to recycler view of profileFragment*/
        postsRecyclerView.setLayoutManager(layoutManager);

        /*init post list*/
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        /*query*/
        final Query query= ref.orderByChild("uid").equalTo(uid);

        /*get All data of the matched query*/
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPost modelPost= snapshot.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    /*add to list*/
                    postList.add(modelPost);

                    /*adapter setting */
                    adapterAddPost= new AdapterAddPost(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterAddPost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
