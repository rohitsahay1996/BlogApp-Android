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


public class LoginActivity extends AppCompatActivity {

    private Button mLogin, mAccount;
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mLogin = (Button) findViewById(R.id.login_btn);
        mAccount = (Button) findViewById(R.id.login_reg_btn);
        mEmail = (EditText) findViewById(R.id.reg_email);
        mPassword = (EditText) findViewById(R.id.reg_pass);
        mProgress = (ProgressBar) findViewById(R.id.login_progress);

        mProgress.setVisibility(View.INVISIBLE);

        mAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) ){
                    mProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                sendToMain();

                            }else {
                                mProgress.setVisibility(View.INVISIBLE);
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: "+errorMessage, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                else{
                    Toast.makeText(LoginActivity.this, "Fields cannot be empty!!", Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.INVISIBLE);
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
           sendToMain();


        }
    }

    private void sendToMain() {

        Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i1);


        finish();
    }
}
