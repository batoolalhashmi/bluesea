package com.barmej.bluesea.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.bluesea.R;
import com.barmej.bluesea.activities.TripDetailsActivity;
import com.barmej.bluesea.adapter.TripsListAdapter;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TripsListFragment extends Fragment implements TripsListAdapter.OnTripClickListener {
    private RecyclerView mRecycleViewTrips;
    private TripsListAdapter mTripsListAdapter;
    private ArrayList<Trip> mTrips;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleViewTrips = view.findViewById(R.id.recycler_view_trip);

        mRecycleViewTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        mTrips = new ArrayList<>();

        mTripsListAdapter = new TripsListAdapter(mTrips, TripsListFragment.this);
        mRecycleViewTrips.setAdapter(mTripsListAdapter);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("trips")
                .orderByChild("date");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTrips.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mTrips.add(ds.getValue(Trip.class));
                }
                mTripsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(getContext(), TripDetailsActivity.class);
        intent.putExtra(TripDetailsActivity.TRIP_DATA, trip);
        startActivity(intent);
    }
}
