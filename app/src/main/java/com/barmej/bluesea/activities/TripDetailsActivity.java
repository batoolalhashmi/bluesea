package com.barmej.bluesea.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.barmej.bluesea.R;
import com.barmej.bluesea.domain.entity.Trip;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TRIP_DATA = "trip_data";
    public static final String TRIP_REF = "trips";
    private Trip intentTrip;
    private Trip trip;
    private String id;
    private String tripAvailableSeatsBeforeBooked;
    private String tripAvailableSeatsAfterBooked;
    private String tripAvailableSeats;
    private String tripBookedUpSeatsBeforeBooked;
    private String tripBookedUpSeatsAfterBooked;
    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;

    private GoogleMap mMap;
    private Marker pickUpMarker;
    private Marker destinationMarker;
    private Marker captainMarker;
    private MapView mMapView;
    private String stringUserId;
    private List<String> userIds;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        final TextView tripPickUpPortTextView = findViewById(R.id.pick_up_port);
        final TextView tripDestinationPortTextView = findViewById(R.id.destination_port);
        final TextView tripDateTextView = findViewById(R.id.date);
        final TextView tripAvailableSeatsTextView = findViewById(R.id.available_seats);
        final MaterialButton reserveTrip = findViewById(R.id.reserve_button);
        reserveTrip.setClickable(true);
        userIds = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference(TRIP_REF);

        if (getIntent() != null && getIntent().getExtras() != null) {
            intentTrip = (Trip) getIntent().getExtras().getSerializable(TRIP_DATA);

            if (intentTrip != null) {
                id = intentTrip.getId();

                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                stringUserId = firebaseUser.getUid();

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        trip = dataSnapshot.child(id).getValue(Trip.class);

                        tripAvailableSeats = trip.getAvailableSeats();
                        int seats = Integer.parseInt(tripAvailableSeats);
                        if (seats == 0 || trip.getStatus().equals(Trip.Status.ARRIVED.name()) || trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                            reserveTrip.setClickable(false);
                        } else {
                            reserveTrip.setClickable(true);
                        }
                        tripPickUpPortTextView.setText(trip.getPickUpPort());
                        tripDestinationPortTextView.setText(trip.getDestinationPort());
                        tripDateTextView.setText(trip.getFormattedDate());
                        tripAvailableSeatsTextView.setText(tripAvailableSeats);

                        if (trip.getUserIds() != null) {
                            List<String> userIds = trip.getUserIds();
                            if (trip.getStatus().equals(Trip.Status.ARRIVED.name()) && userIds.contains(stringUserId)) {
                                showArrivedDialog();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
            }
        }


        reserveTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tripAvailableSeatsBeforeBooked = trip.getAvailableSeats();
                int tripAvailableSeatsNo = Integer.parseInt(tripAvailableSeatsBeforeBooked) - 1;
                tripAvailableSeatsAfterBooked = String.valueOf(tripAvailableSeatsNo);
                trip.setAvailableSeats(tripAvailableSeatsAfterBooked);

                tripBookedUpSeatsBeforeBooked = trip.getBookedUpSeats();
                int tripBookedUpSeatsNo = Integer.parseInt(tripBookedUpSeatsBeforeBooked) + 1;
                tripBookedUpSeatsAfterBooked = String.valueOf(tripBookedUpSeatsNo);
                trip.setBookedUpSeats(tripBookedUpSeatsAfterBooked);

                if (trip.getUserIds() != null) {
                    userIds = trip.getUserIds();
                }
                userIds.add(stringUserId);
                trip.setUserIds(userIds);

                mDatabase.child(id).setValue(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(TripDetailsActivity.this, R.string.reserve_done, Toast.LENGTH_SHORT).show();
                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Trip availableSeatTrip = dataSnapshot.child(id).getValue(Trip.class);
                                    tripAvailableSeatsTextView.setText(availableSeatTrip.getAvailableSeats());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(TripDetailsActivity.this, R.string.reserve_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.child(id).getValue(Trip.class);
                mMap = googleMap;
                updateMarkers(trip);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateMarkers(Trip trip) {
        if (trip != null) {
            LatLng captainLatLng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());
            LatLng pickUpLatLng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
            LatLng destinationLatLng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());

            setCaptainMarker(captainLatLng);
            setPickUpMarker(pickUpLatLng);
            setDestinationMarker(destinationLatLng);

            showCaptainCurrentLocationOnMap(captainLatLng);
        }
    }

    private void showArrivedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.you_have_arrived);
        builder.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    public void setCaptainMarker(LatLng target) {
        if (mMap == null) return;
        if (captainMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.boat);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            captainMarker = mMap.addMarker(options);
        } else {
            captainMarker.setPosition(target);
        }
    }

    public void setPickUpMarker(LatLng target) {
        if (mMap == null) return;
        if (pickUpMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.position);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            pickUpMarker = mMap.addMarker(options);
        } else {
            pickUpMarker.setPosition(target);
        }
    }

    public void setDestinationMarker(LatLng target) {
        if (mMap == null) return;
        if (destinationMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.destination);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);

            destinationMarker = mMap.addMarker(options);
        } else {
            destinationMarker.setPosition(target);
        }
    }

    private void showCaptainCurrentLocationOnMap(LatLng captainLatLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(captainLatLng, 3f);
        mMap.moveCamera(update);
    }
}