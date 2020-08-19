package com.barmej.bluesea.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.barmej.bluesea.R;
import com.barmej.bluesea.databinding.ActivityTripDetailsBinding;
import com.barmej.bluesea.domain.entity.Trip;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TRIP_DATA = "trip_data";
    public static final String TRIP_REF = "trips";
    public static final String USER_REF = "users";
    private static final String FORMATTED_DATE = "formattedDate";
    private ActivityTripDetailsBinding binding;
    private Trip intentTrip;
    private Trip trip;
    private String id;
    private String tripAvailableSeatsBeforeBooked;
    private String tripAvailableSeatsAfterBooked;
    private String tripAvailableSeats;
    private String tripBookedUpSeatsBeforeBooked;
    private String tripBookedUpSeatsAfterBooked;
    DatabaseReference mDatabase , userDatabase;
    FirebaseUser firebaseUser;
    private Trip checkTrip;
    private String stringDate;

    private GoogleMap mMap;
    private Marker pickUpMarker;
    private Marker destinationMarker;
    private Marker captainMarker;
    private String stringUserId;
    private List<String> userIds;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trip_details);

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        binding.reserveButton.setClickable(true);
        userIds = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference(TRIP_REF);
        userDatabase = FirebaseDatabase.getInstance().getReference(USER_REF);


        if (getIntent() != null && getIntent().getExtras() != null) {
            intentTrip = (Trip) getIntent().getExtras().getSerializable(TRIP_DATA);

            if (intentTrip != null) {
                stringDate = intentTrip.getFormattedDate();
                id = intentTrip.getId();

                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                stringUserId = firebaseUser.getUid();


                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        trip = dataSnapshot.child(id).getValue(Trip.class);

                        tripAvailableSeats = trip.getAvailableSeats();
                        int seats = Integer.parseInt(tripAvailableSeats);
                        if (trip.getUserIds() != null) {
                            userIds = trip.getUserIds();
                            if (userIds.contains(stringUserId)) {
                                binding.reserveButton.setClickable(false);
                                binding.reserveButton.setText(getString(R.string.booked));
                                binding.reserveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                            }
                        } else if (seats == 0) {
                            binding.reserveButton.setClickable(false);
                            binding.reserveButton.setText(getString(R.string.no_seat_available));
                            binding.reserveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        } else if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                            binding.reserveButton.setClickable(false);
                            binding.reserveButton.setText(getString(R.string.trip_not_available));
                            binding.reserveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        } else if (trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                            binding.reserveButton.setClickable(false);
                            binding.reserveButton.setText(getString(R.string.trip_start));
                            binding.reserveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        } else {
                            binding.reserveButton.setClickable(true);
                        }
                        binding.pickUpPort.setText(trip.getPickUpPort());
                        binding.destinationPort.setText(trip.getDestinationPort());
                        binding.date.setText(trip.getFormattedDate());
                        binding.availableSeats.setText(tripAvailableSeats);

                        if (trip.getUserIds() != null) {
                            List<String> userIds = trip.getUserIds();
                            if (trip.getStatus().equals(Trip.Status.ARRIVED.name()) && userIds.contains(stringUserId)) {
                                if (!((TripDetailsActivity.this).isFinishing())) {
                                    binding.reserveButton.setClickable(false);
                                    binding.reserveButton.setText(getString(R.string.you_have_arrived));
                                    binding.reserveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TripDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

                    }

                });


            }
            Query query = mDatabase.orderByChild(FORMATTED_DATE)
                    .equalTo(stringDate);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        checkTrip = ds.getValue(Trip.class);
                        if (checkTrip != null && !checkTrip.getId().equals(trip.getId())) {
                            if (checkTrip.getUserIds() != null && checkTrip.getUserIds().contains(stringUserId) && trip.getStatus().equals(Trip.Status.MOVING_SOON.name())) {
                                Toast.makeText(TripDetailsActivity.this, R.string.can_not_book_trip_in_same_date, Toast.LENGTH_LONG).show();
                                binding.reserveButton.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(TripDetailsActivity.this, R.string.book_trip_failed, Toast.LENGTH_SHORT).show();

                }
            });
        }


        binding.reserveButton.setOnClickListener(new View.OnClickListener() {
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
                                    binding.availableSeats.setText(availableSeatTrip.getAvailableSeats());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(TripDetailsActivity.this, R.string.reserve_failed, Toast.LENGTH_SHORT).show();

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
        mMap = googleMap;
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.child(id).getValue(Trip.class);
                LatLng pickUpLatLng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
                LatLng destinationLatLng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());
                setPickUpMarker(pickUpLatLng);
                setDestinationMarker(destinationLatLng);
                LatLng captainLatLng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());
                setCaptainMarker(captainLatLng);
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(captainLatLng, 10f);
                mMap.moveCamera(update);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.mapView.onStop();
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
        CameraUpdate update = CameraUpdateFactory.newLatLng(captainMarker.getPosition());
        mMap.moveCamera(update);
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
}