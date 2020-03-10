package com.example.firebaseapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;

    /*xml Views*/
    EditText titleEt,descriptionEt;
    ImageView postImageView;
    Button uploadBtn;

    /*Permissions Constants*/
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int  STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    /*Arrays of Permission to be requested*/
    String cameraPermissions[];
    String storagePermissions[];

    /*Image URI*/
    Uri image_uri=null;

    /*Firebase*/
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    /*Strings*/
    String name, phone, email, profileImage, coverImage,uid;

    /*progressBar*/
    ProgressDialog progressDialog;

    /*Post Edit Section*/
    /*info of post to be edited*/
    String editTitle,editDescription,editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Add New Post");
        /*enable back button in action bar*/
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        init();
        checkUserStatus();
        /*get Some info of Current user to include in post*/
        databaseReference=FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog=new ProgressDialog(this);
        actionBar.setTitle(email);

        performingQuery();
        /*get Image From Gallery/Camera*/
        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        /*Edit Post Section*/
        /*getting data through intent that is passed from AdapterAddPost.java*/
        Intent intent=getIntent();
        final String isUpdateKey= ""+intent.getStringExtra("key");
        final String editPostId= ""+intent.getStringExtra("editPostId");

        /*Validate if we came here to update or edit not for first time add post*/
        if(isUpdateKey.equals("editPost")){
            /*update or edit post*/
            actionBar.setTitle("Edit Post");
            uploadBtn.setText("Update");
            load_postData(editPostId);

        }else{
            /*add new post*/
            actionBar.setTitle("Add new post");
            uploadBtn.setText("Upload");
        }

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*get title,description from editTExt*/
                String title=titleEt.getText().toString();
                String description=descriptionEt.getText().toString();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost")) {
                    beginUpdate(title,description,editPostId);
                }else{
                    uploadData(title,description);
                }


            }
        });

    }

    private void beginUpdate(String title, String description, String editPostId) {

        progressDialog.setMessage("Updating post...");
        progressDialog.show();
        if (!editImage.equals("noImage")) {
            /*already there is post image first dlete then upload new one*/
            update_withImage(title, description, editPostId);
        } else if(postImageView.getDrawable()!=null) {
            /*there is no post image just title and description ,now upload image update image*/
            update_withImageNow(title, description, editPostId);
        }else{
            /*if you dont want to upload any post image*/
            /*without image*/
            update_withouImage(title,description,editPostId);
        }
    }

    private void update_withouImage(String title, String description, String editPostId) {

        HashMap<String,Object> hashMap= new HashMap<>();
        /*put post info*/
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("email",email);
        hashMap.put("uDp",profileImage);
        hashMap.put("pTitle",title);
        hashMap.put("pDescription",description);
        hashMap.put("pImage","noImage");

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        databaseReference.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void update_withImageNow(final String title, final String description, final String editPostId) {

        /*for post-image name ,postId,publish Time*/
        String timeStamp= String.valueOf(System.currentTimeMillis());
        String fileNameAndPath= "Posts/"+"posts_"+timeStamp;

        /*get image from image View*/
        Bitmap bitmap = ((BitmapDrawable)postImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        /*image compress*/
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        final byte[] data= byteArrayOutputStream.toByteArray();

        /*create new storage reference child*/
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                /*image upload get its url*/
                Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadUri= uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    /*uri received upload to firebase database*/
                    HashMap<String,Object> hashMap= new HashMap<>();
                    /*put post info*/
                    hashMap.put("uid",uid);
                    hashMap.put("uName",name);
                    hashMap.put("email",email);
                    hashMap.put("uDp",profileImage);
                    hashMap.put("pTitle",title);
                    hashMap.put("pDescription",description);
                    hashMap.put("pImage",downloadUri);

                    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
                    databaseReference.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                /*not uploaded got some error*/
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void update_withImage(final String title, final String description, final String editPostId) {

        /*if you want to edit a post that has already image then first delete that image first then update*/
        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                /*image deleted now upload new image*/
                /*for post-image name ,postId,publish Time*/
                String timeStamp= String.valueOf(System.currentTimeMillis());
                String fileNameAndPath= "Posts/"+"posts_"+timeStamp;

                /*get image from image View*/
                Bitmap bitmap = ((BitmapDrawable)postImageView.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                /*image compress*/
                bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                final byte[] data= byteArrayOutputStream.toByteArray();

                /*create new storage reference child*/
                StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /*image upload get its url*/
                        Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri= uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            /*uri received upload to firebase database*/
                            HashMap<String,Object> hashMap= new HashMap<>();
                            /*put post info*/
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("email",email);
                            hashMap.put("uDp",profileImage);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescription",description);
                            hashMap.put("pImage",downloadUri);

                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
                            databaseReference.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        /*not uploaded got some error*/
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void load_postData(String editPostId) {

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
        /*get Detail of post using id of post*/
        Query query=reference.orderByChild("pId").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    /*getData*/
                    editTitle= ""+snapshot.child("pTitle").getValue();
                    editDescription= ""+snapshot.child("pDescription").getValue();
                    editImage= ""+snapshot.child("pImage").getValue();

                    /*setData to the views*/
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);
                    /*setImage*/
                    /*check wether post has image or not*/
                    if(!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(postImageView);
                        }catch (Exception e){

                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /*initializing views of xml*/
    private void init() {
        titleEt=findViewById(R.id.postTitleET);
        descriptionEt=findViewById(R.id.postImageDescriptionET);
        postImageView=findViewById(R.id.postImageView);
        uploadBtn=findViewById(R.id.postUploadButton);

        /*initializing permissions of Arrays*/
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }
    /*wether user logged in or not*/
    private void checkUserStatus(){
        Intent intent;
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            /*user is signed in stay here*/
            email=firebaseUser.getEmail();
            uid=firebaseUser.getUid();


        }else{
            intent=new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    /*where to pick image from*/
    public void showImagePicDialog() {

        /*Showing Dialog Containing Camera & Gallery
         * Gallery and Camera Image take Permission Required*/
        String options[] = {"Camera", "Gallery"};
        /*Alert Dialog*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
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
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        /*Requesting for Storage Permission on Runtime*/
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }
    private boolean checkCamerPermission() {
        /*Checking if Camera Permission is Enabled or Not
        return true if Enabled otherwise return false*/
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
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
                        Toast.makeText(this, "Please enabled storage and camera permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Please enabled storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    /*After Getting Permissios*/
    /*Take pic from Camera*/
    private void pickFromCamera() {
        /*Intent of picking image from device camera*/
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        /*Put image URI*/
        image_uri=this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
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
    /*This Method is called after picking image from gallery or from camera*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE){
                //image is picked from gallery get uri of image
                image_uri=data.getData();
                postImageView.setImageURI(image_uri);
                //Toast.makeText(getActivity(), "IMAGE_PICK_GALLERY_REQUEST_CODE", Toast.LENGTH_LONG).show();
            }
            if(requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                //image is picked from camera get uri of image
                postImageView.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /*searching user according to email*/
    private void performingQuery() {
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
    }
    /*getting userData to add in post like name ,email, uid*/
    public void GetnSetUserDetail(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Map<Object, String> data = (Map<Object, String>) snapshot.getValue();
            /*Getting data from FirebaseDatabase*/
            name = data.get("name");
            email = data.get("email");
            profileImage = data.get("profileImage");
        }
    }

    /*After getting permissions & Getting current user Data(name,email,profileImage) to show with Posts...Time to upload data on Firebase*/
    /*uploadData of addPost to Firebase*/
    private void uploadData(final String title, final String description) {
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();
        /*for post image,name,post_id,post_publish time*/
        final String timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName= "Posts/"+"post_"+timeStamp;
        if(postImageView.getDrawable() != null){
            /*get image from image View*/
            Bitmap bitmap = ((BitmapDrawable)postImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
            /*image compress*/
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] data= byteArrayOutputStream.toByteArray();

            /*post with Image*/
            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /*image is uploaded to firebase storage,now gets its uri*/
                    Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String downloadUri= uriTask.getResult().toString();
                    if(uriTask.isSuccessful()){
                        /*uri is received upload post to firebase database*/
                        HashMap<Object,String> hashMap=new HashMap<>();
                        hashMap.put("uid",uid);
                        hashMap.put("uName",name);
                        hashMap.put("email",email);
                        hashMap.put("uDp",profileImage);
                        hashMap.put("pId",timeStamp);
                        hashMap.put("pTitle",title);
                        hashMap.put("pDescription",description);
                        hashMap.put("pImage",downloadUri);
                        hashMap.put("pTime",timeStamp);
                        /*path to store data*/
                        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        /*put data in this reference*/
                        databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /*added in database*/
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post Published!", Toast.LENGTH_SHORT).show();
                                /*reset Views*/
                                titleEt.setText("");
                                descriptionEt.setText("");
                                postImageView.setImageURI(null);
                                image_uri=null;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                /*failed adding post in database*/
                                progressDialog.show();
                                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    /*failed to upload*/
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            /*post without Image*/
            HashMap<Object,String> hashMap=new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("email",email);
            hashMap.put("uDp",profileImage);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescription",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);

            /*path to store data*/
            databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
            /*put data in this reference*/
            databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    /*added in database*/
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post Published!", Toast.LENGTH_SHORT).show();
                    /*reset Views*/
                    titleEt.setText("");
                    descriptionEt.setText("");
                    postImageView.setImageURI(null);
                    image_uri=null;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    /*failed adding post in database*/
                    progressDialog.show();
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /*menu & start,resume methods*/
    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        /*hide the addPost in this Activity*/
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
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
}
