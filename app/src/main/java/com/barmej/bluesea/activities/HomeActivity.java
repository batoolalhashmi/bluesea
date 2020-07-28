package com.barmej.bluesea.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.barmej.bluesea.R;
import com.barmej.bluesea.domain.entity.Trip;
import com.barmej.bluesea.fragments.CurrentTripFragment;
import com.barmej.bluesea.fragments.TripsListFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.text.DateFormat.getDateInstance;

public class HomeActivity extends AppCompatActivity {
    private static final String TRIP_REF_PATH = "trips";
    private static final String FORMATTED_DATE = "formattedDate";
    private TripsListFragment tripsListFragment;
    private String userName;
    private String userPhoto;
    private CurrentTripFragment currentTripInfoFragment;
    private FrameLayout currentTripFragmentFrameLayout;
    private DatabaseReference mDatabase;
    private Query query;
    private Trip trip;
    FirebaseUser firebaseUser;
    FragmentTransaction fragmentTransaction;
    private String stringDate;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentTripInfoFragment = new CurrentTripFragment();
        currentTripFragmentFrameLayout = findViewById(R.id.fragment_current_trip);

        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
        registerReceiver(m_timeChangedReceiver, new IntentFilter(Intent.ACTION_TIME_CHANGED));

        final Calendar calendar = Calendar.getInstance();
        final Date currentDate = calendar.getTime();
        updateCurrentFragment(currentDate);
    }

    private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Date currentDate = new Date();
            if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK)) {
                updateCurrentFragment(currentDate);
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }

    private void updateCurrentFragment(Date currentDate) {
        currentTripFragmentFrameLayout.setVisibility(View.GONE);
        tripsListFragment = (TripsListFragment) getSupportFragmentManager().findFragmentById(R.id.trips_list_fragment);
        stringDate = getDateInstance(DateFormat.MEDIUM).format(currentDate);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        query = mDatabase.child(TRIP_REF_PATH).orderByChild(FORMATTED_DATE)
                .equalTo(stringDate);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                    if (trip != null && trip.getUserIds() != null && trip.getUserIds().contains(firebaseUser.getUid())) {
                        currentTripFragmentFrameLayout.setVisibility(View.VISIBLE);
                        if (!isFinishing()) {
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
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
                    } else {
                        currentTripFragmentFrameLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

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

