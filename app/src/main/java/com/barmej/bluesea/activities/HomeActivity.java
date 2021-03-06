package com.barmej.bluesea.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.barmej.bluesea.R;
import com.barmej.bluesea.domain.entity.Trip;
import com.barmej.bluesea.fragments.CurrentTripFragment;
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
    private static final String TRIP_REF_PATH = "trips";
    private String userName;
    private String userPhoto;
    private static FrameLayout currentTripFragmentFrameLayout;
    private DatabaseReference mDatabase;
    private Trip trip;
    FirebaseUser firebaseUser;
    FragmentTransaction fragmentTransaction;
    private static final String ON_TRIP = "ON_TRIP";
    private static final String STATUS = "status";

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentTripFragmentFrameLayout = findViewById(R.id.fragment_current_trip);
        updateCurrentFragment();
    }

    public static void showCurrentLayout(boolean showCurrentLayout) {
        if (showCurrentLayout) {
            currentTripFragmentFrameLayout.setVisibility(View.VISIBLE);
        } else {
            currentTripFragmentFrameLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCurrentFragment() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference(TRIP_REF_PATH);
        mDatabase.orderByChild(STATUS).equalTo(ON_TRIP).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                    if (trip != null && trip.getUserIds() != null && trip.getUserIds().contains(firebaseUser.getUid())) {
                        if (!isFinishing()) {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            CurrentTripFragment currentTripInfoFragment = new CurrentTripFragment();
                            fragmentTransaction.replace(R.id.fragment_current_trip, currentTripInfoFragment).commitAllowingStateLoss();
                        }
                        currentTripFragmentFrameLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(HomeActivity.this, TripDetailsActivity.class);
                                intent.putExtra(TripDetailsActivity.TRIP_DATA, trip);
                                startActivity(intent);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem menuItem = menu.findItem(R.id.profile_setting);
        View imageView = menuItem.getActionView();
        final CircleImageView profileImage = imageView.findViewById(R.id.toolbar_profile_image);

        final MenuItem menuTextItem = menu.findItem(R.id.menu_user_name);
        userName = getIntent().getStringExtra(LoginActivity.USER_NAME);
        userPhoto = getIntent().getStringExtra(LoginActivity.USER_PHOTO);
        menuTextItem.setTitle(userName);
        Glide.with(profileImage)
                .load(userPhoto).into(profileImage);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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

