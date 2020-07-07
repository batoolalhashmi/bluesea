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
    private static final String USER_REF_PATH = "users";
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

        tripsListFragment = new TripsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            fragmentTransaction.replace(R.id.main_layout, tripsListFragment, SAVED_FRAGMENT).commit();
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


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(USER_REF_PATH);
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
        userName.setTitle(getString(R.string.user_name));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, SignUpActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

