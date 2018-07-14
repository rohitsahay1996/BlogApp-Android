package com.example.rohitsahay.mydesign;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private FloatingActionButton mAddPost;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private BottomNavigationView mMainBottomNav;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        firebaseFirestore = FirebaseFirestore.getInstance();
        getSupportActionBar().setTitle("Photo Blog");




        if(mAuth.getCurrentUser() != null) {

            mMainBottomNav = findViewById(R.id.main_bottom_nav);
            //FRAGMENTS
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            ReplaceFragment(homeFragment);



            mMainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                    switch (item.getItemId()) {
                        case R.id.botom_action_home:
                            ReplaceFragment(homeFragment);
                            return true;

                        case R.id.botom_action_account:
                            ReplaceFragment(accountFragment);
                            return true;

                        case R.id.botom_action_notif:
                            ReplaceFragment(notificationFragment);
                            return true;

                        default:
                            return false;


                    }


                }
            });

            mAddPost = findViewById(R.id.addPostBtn);

            mAddPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newpost_intent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newpost_intent);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
       sendToLogin();
        }else{
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent setup_intent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setup_intent);
                            finish();

                        }

                    }else{

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "error = "+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.toolbar_logout:
                logOut();
                return true;

            case R.id.toolbar_settings:
                Intent setup_intent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(setup_intent);
                return true;


            default:
                return false;

        }

    }

    private void logOut() {

        mAuth.signOut();
        sendToLogin();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

       getMenuInflater().inflate(R.menu.main_menu,menu) ;

        return true;

    }
    private void sendToLogin() {
        Intent i = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(i);


        finish();
    }
    private void ReplaceFragment(android.support.v4.app.Fragment fragment){

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();



    }
}
