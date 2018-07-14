package com.example.rohitsahay.mydesign;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private CircleImageView setupImage;
    private Uri mainImageUri = null;
    private EditText mName;
    private Button mSetupBtn;
    private ProgressBar mProgress;
    private boolean isChanged = false;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private StorageReference storagerefrence;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mName = findViewById(R.id.setup_name);
        mSetupBtn = findViewById(R.id.setup_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        mProgress = findViewById(R.id.setup_progress);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storagerefrence = FirebaseStorage.getInstance().getReference();
        mToolbar = (Toolbar)findViewById(R.id.account_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setup");

        user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        mName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);

                       // Toast.makeText(SetupActivity.this, "Data Exists!!", Toast.LENGTH_SHORT).show();

                    }else{

                        //Toast.makeText(SetupActivity.this, "Data doesn't exists", Toast.LENGTH_SHORT).show();
                    }


                }else{

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "error = "+error, Toast.LENGTH_SHORT).show();

                }
            }

        });


        //Setup Button

        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = mName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {
                    mProgress.setVisibility(View.VISIBLE);
                if(isChanged == true) {

                    //store image in firebase storage
                        StorageReference imagePath = storagerefrence.child("Profile Images").child(user_id + ".jpg");
                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);

                                } else {
                                    String Imageerror = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "error = " + Imageerror, Toast.LENGTH_SHORT).show();

                                }


                            }
                        });

                    }
                else{
                    storeFirestore(null,user_name);
                }
                }
            }
        });



        setupImage = findViewById(R.id.setup_default_img);


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //*******Select image from storage*******
                /*checking the version is greater than marshmello or not
                if marshmello then user need to give permission implicitly*/
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                       // Toast.makeText(SetupActivity.this, "Permission Denied!!", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    }
                    else {
                        //it will send user to choose and crop image from gallery or camera
                        BringImagePicker();
                    }
                }else{

                    BringImagePicker();

                }

            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task , String user_name) {

    Uri download_uri;
        if(task != null ) {
             download_uri = task.getResult().getDownloadUrl();
        }else{
                download_uri = mainImageUri;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgress.setVisibility(View.INVISIBLE);
                    Intent main_intent = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(main_intent);
                    finish();

                    Toast.makeText(SetupActivity.this, "Profile Completed!!", Toast.LENGTH_SHORT).show();

                }else{

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "error = "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
