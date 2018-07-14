package com.example.rohitsahay.mydesign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageView mPostImg;
    private EditText mDisc;
    private Button mPost;
    private Uri postImgUri;
    private ProgressBar mNewprogress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.new_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Photo Blog");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNewprogress = findViewById(R.id.new_post_Progress);
        mPostImg = findViewById(R.id.newPostImg);
        mDisc = findViewById(R.id.addDiscription);
        mPost = findViewById(R.id.postBtn);

        mPostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                        final String desc = mDisc.getText().toString();
                        if(!TextUtils.isEmpty(desc) && postImgUri != null){
                            mNewprogress.setVisibility(View.VISIBLE);

                            final String randomName = UUID.randomUUID().toString();
                            StorageReference filePath = storageReference.child("post_images").child(randomName);
                            filePath.putFile(postImgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                    final String downloadUri = task.getResult().getDownloadUrl().toString();
                                    if(task.isSuccessful()){
                                        File newImageFile = new File(postImgUri.getPath());

                                        try {
                                            compressedImageFile = new Compressor(NewPostActivity.this)
                                                    .setMaxHeight(200)
                                                    .setMaxWidth(200)
                                                    .setQuality(10)
                                                    .compressToBitmap(newImageFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                        byte[] thumbData = baos.toByteArray();


                                        UploadTask uploadTask = storageReference.child("post_images/thumbs").
                                                child(randomName+".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();

                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url",downloadUri);
                                                postMap.put("thumb_url",downloadThumbUri);
                                                postMap.put("desc",desc);
                                                postMap.put("user_id",current_user_id);
                                                postMap.put("timestamp",FieldValue.serverTimestamp());



                                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {


                                                        if(task.isSuccessful()){

                                                            Toast.makeText(NewPostActivity.this, "Post was Added!!", Toast.LENGTH_SHORT).show();

                                                            Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();

                                                        }else{


                                                        }
                                                        mNewprogress.setVisibility(View.INVISIBLE);
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                //Error Handling
                                             //   Exception error = result.getError();
                                               // Toast.makeText(NewPostActivity.this, "error = "+error, Toast.LENGTH_SHORT).show();


                                            }
                                        });




                                    }else{
                                        mNewprogress.setVisibility(View.INVISIBLE);
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(NewPostActivity.this, "error = "+errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }else{

                            Toast.makeText(NewPostActivity.this, "Field cannot be empty!!", Toast.LENGTH_SHORT).show();
                        }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImgUri = result.getUri();
                mPostImg.setImageURI(postImgUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(NewPostActivity.this, "error = "+error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
