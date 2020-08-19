package com.barmej.bluesea.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.bluesea.R;
import com.barmej.bluesea.activities.TripDetailsActivity;
import com.barmej.bluesea.adapter.TripsListAdapter;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.barmej.bluesea.activities.HomeActivity.showCurrentLayout;

public class TripsListFragment extends Fragment implements TripsListAdapter.OnTripClickListener {
    private static final String TRIP_REF_PATH = "trips";
    private static final String DATE = "date";
    private static final String STATUS = "status";
    private static final String ON_TRIP = "ON_TRIP";
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private Parcelable savedRecyclerLayoutState;
    private RecyclerView mRecycleViewTrips;
    private TripsListAdapter mTripsListAdapter;
    private ArrayList<Trip> mTrips;
    private DatabaseReference mDatabase;
    private FirebaseUser firebaseUser;
    private Trip trip;
    private LinearLayoutManager mLinearLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleViewTrips = view.findViewById(R.id.recycler_view_trip);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecycleViewTrips.setLayoutManager(mLinearLayoutManager);
        mTrips = new ArrayList<>();

        mTripsListAdapter = new TripsListAdapter(mTrips, TripsListFragment.this);
        mRecycleViewTrips.setAdapter(mTripsListAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        updateCurrentFragment(firebaseUser.getUid());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child(TRIP_REF_PATH)
                .orderByChild(DATE);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTrips.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mTrips.add(ds.getValue(Trip.class));
                }
                mTripsListAdapter.notifyDataSetChanged();
                if (savedRecyclerLayoutState != null) {
                    mLinearLayoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void updateCurrentFragment(final String userId) {
        FirebaseDatabase.getInstance().getReference(TRIP_REF_PATH).orderByChild(STATUS).equalTo(ON_TRIP).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showCurrentLayout(false);
                    return;
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                    if (trip != null && trip.getUserIds() != null && trip.getUserIds().contains(userId)) {
                        showCurrentLayout(true);
                    } else {
                        showCurrentLayout(false);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT,
                mLinearLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(getContext(), TripDetailsActivity.class);
        intent.putExtra(TripDetailsActivity.TRIP_DATA, trip);
        startActivity(intent);
    }

}
