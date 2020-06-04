package com.barmej.bluesea.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.barmej.bluesea.R;
import com.barmej.bluesea.fragments.TripsListFragment;
import com.barmej.bluesea.domain.entity.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    private static final String SAVED_FRAGMENT = "fragment";
    private TripsListFragment tripsListFragment;
    private String userName;
    private String userPhoto;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            tripsListFragment = new TripsListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_layout, tripsListFragment, SAVED_FRAGMENT).commit();
        } else {
            tripsListFragment = (TripsListFragment) getSupportFragmentManager().findFragmentByTag(SAVED_FRAGMENT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.profile_setting);
        View imageView = MenuItemCompat.getActionView(menuItem);
        final CircleImageView profileImage = imageView.findViewById(R.id.toolbar_profile_image);

        final MenuItem menuTextItem = menu.findItem(R.id.menu_user_name);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String firebaseUserId = firebaseUser.getUid();


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.child(firebaseUserId).getValue(User.class);
                if (user != null) {
                    userName = user.getUserName();
                    userPhoto = user.getUserPhoto();
                    menuTextItem.setTitle(userName);
                    Glide.with(profileImage)
                            .load(userPhoto).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        MenuItem userName = menu.findItem(R.id.menu_user_name);
        userName.setTitle("ahmed");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    startActivity(new Intent(HomeActivity.this, SignUpActivity.class));
                    finish();
                } else {
                    Toast.makeText(HomeActivity.this, "can't sign out", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

