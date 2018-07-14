package com.example.rohitsahay.mydesign;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail,mPassword,mConformPass;
    private Button mRegBtn,mLoginBtn;
    private ProgressBar reg_progress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mEmail= (EditText) findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_pass);
        mConformPass = findViewById(R.id.reg_conform_pass);
        mRegBtn = findViewById(R.id.reg_btn);
        mLoginBtn = findViewById(R.id.login_reg_btn);
        reg_progress = findViewById(R.id.reg_progress);


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(login_intent);
                finish();
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();
                String confirmPass = mConformPass.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){

                    if(pass.equals(confirmPass)){
                        reg_progress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
                               //send to setup activity
                               Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                               startActivity(setupIntent);
                               finish();  //if press back buton do not come bake , exit

                               reg_progress.setVisibility(View.INVISIBLE);

                           }else {
                               String errorMessage = task.getException().getMessage();
                               Toast.makeText(RegisterActivity.this, "error = "+errorMessage, Toast.LENGTH_SHORT).show();
                               reg_progress.setVisibility(View.INVISIBLE);

                           }
                            }
                        });

                    }
                    else Toast.makeText(RegisterActivity.this, "Password doesn't match!!", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(RegisterActivity.this, "Field cannot be empty!!", Toast.LENGTH_SHORT).show();
            }
        });
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user = mAuth.getCurrentUser();
        if(current_user != null){
            sendToMain();
        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
