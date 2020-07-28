package com.barmej.bluesea.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barmej.bluesea.R;
import com.barmej.bluesea.activities.TripDetailsActivity;
import com.barmej.bluesea.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.text.DateFormat.getDateInstance;

public class CurrentTripFragment extends Fragment {
    private static final String TRIP_REF_PATH = "trips";
    private static final String FORMATTED_DATE = "formattedDate";
    private Trip trip;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_trip, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView tripPickUpPortTextView = view.findViewById(R.id.pick_up_port_text_view);
        final TextView tripDestinationPortTextView = view.findViewById(R.id.destination_port_text_view);
        final Calendar calendar = Calendar.getInstance();
        final Date date = calendar.getTime();
        String stringDate = getDateInstance(DateFormat.MEDIUM).format(date);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child(TRIP_REF_PATH).orderByChild(FORMATTED_DATE)
                .equalTo(stringDate);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    trip = ds.getValue(Trip.class);
                }
                if (trip != null) {
                    tripPickUpPortTextView.setText(trip.getPickUpPort());
                    tripDestinationPortTextView.setText(trip.getDestinationPort());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
