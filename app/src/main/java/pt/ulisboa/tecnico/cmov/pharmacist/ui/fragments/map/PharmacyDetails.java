package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.QRCodeActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.RecyclerAdapterProvider;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders.MedicineViewHolder;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AdapterUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.NavigateFunction;


public class PharmacyDetails {
    private final TextView title, location, distance;
    private final ImageView image;
    private final MaterialButton favouriteButton;
    private Pharmacy currentPharmacy;
    private final Context fragmentContext;
    private final RecyclerAdapterProvider<List<Medicine>, MedicineViewHolder> medicineListAdapter;
    private List<Medicine> medicines = new ArrayList<>();
    private Query currentStockQuery;
    private DatabaseReference currentRatingsRef;
    private ChildEventListener currentStockQueryEventListener;
    private ValueEventListener currentRatingsRefEventListener;
    private Map<String, Boolean> favoritePharmacies;
    private RatingBar userRatingBar;
    private TextView averageRatingTextView;
    private LinearProgressIndicator fiveStarProgressBar;
    private LinearProgressIndicator fourStarProgressBar;
    private LinearProgressIndicator threeStarProgressBar;
    private LinearProgressIndicator twoStarProgressBar;
    private LinearProgressIndicator oneStarProgressBar;

    private static final String TAG = PharmacyDetails.class.getSimpleName();

    public PharmacyDetails(Fragment fragment, View bottomSheetView, SharedLocationViewModel sharedLocationViewModel, NavigateFunction<Medicine> openMedicine) {

        title = bottomSheetView.findViewById(R.id.details_pharmacy_title);
        location = bottomSheetView.findViewById(R.id.details_pharmacy_location);
        distance = bottomSheetView.findViewById(R.id.details_pharmacy_distance);
        image = bottomSheetView.findViewById(R.id.pharmacy_image);
        RecyclerView medicineList = bottomSheetView.findViewById(R.id.fragment_map_avaliable_medicines);
        favouriteButton = bottomSheetView.findViewById(R.id.favouriteButton);

        userRatingBar = bottomSheetView.findViewById(R.id.user_rating_bar);
        averageRatingTextView = bottomSheetView.findViewById(R.id.average_rating);
        fiveStarProgressBar = bottomSheetView.findViewById(R.id.fiveStarProgressBar);
        fourStarProgressBar = bottomSheetView.findViewById(R.id.fourStarProgressBar);
        threeStarProgressBar = bottomSheetView.findViewById(R.id.threeStarProgressBar);
        twoStarProgressBar = bottomSheetView.findViewById(R.id.twoStarProgressBar);
        oneStarProgressBar = bottomSheetView.findViewById(R.id.oneStarProgressBar);

        userRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> submitReview(rating));
        //Initialize user ratings
        resetUserRatings();


        fragmentContext = fragment.getContext();

        // Add Stock Button
        fragment.requireView().findViewById(R.id.details_add_stock_button).setOnClickListener(v -> {
            Intent intent = new Intent(fragmentContext, QRCodeActivity.class);

            if (currentPharmacy == null) return;

            intent.putExtra("pharmacyId", currentPharmacy.getId());
            fragment.startActivity(intent);
        });

        // Register distance observer
        sharedLocationViewModel.getLocation().observe(fragment.getViewLifecycleOwner(), location -> {
            if (currentPharmacy == null) return;
            Float distanceValue = Location.getDistance(location, currentPharmacy.getLocation());
            distance.setText(Location.getDistanceString(distanceValue));
        });

        // Initialize medicines list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(fragment.getActivity());

        medicineListAdapter = new RecyclerAdapterProvider<>(medicines, fragmentContext, R.layout.medicine_list_item, (view, context, dataset) -> new MedicineViewHolder(view, context, dataset, medicine -> openMedicine.apply(medicine, "MedicinesInPharmacyRecyclerAdapter"), this::buyMedicine));

        medicineList.setLayoutManager(mLayoutManager);
        medicineList.setAdapter(medicineListAdapter);

        // Register user updates handler
        AuthUtils.registerUserDataListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user == null) return;

                favoritePharmacies = user.getFavoritePharmaciesIds();
                updateUserActions();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        favouriteButton.setOnClickListener(v -> updateFavorite());

        currentStockQueryEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String medicineKey = snapshot.getKey();

                if (medicineKey != null) {
                    FirebaseDatabase.getInstance().getReference().child("medicines").child(medicineKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Medicine medicine = snapshot.getValue(Medicine.class);

                                medicines.add(medicine);
                                medicineListAdapter.notifyItemInserted(medicines.indexOf(medicine));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, MessageFormat.format("Failed to fetch medicine: ", error.getMessage()));
                        }
                    });

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                AdapterUtils.removeChild(snapshot.getKey(), medicines, medicineListAdapter);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        currentRatingsRefEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Long> ratings = (Map<String, Long>) snapshot.getValue();
                    updateProgressBars(ratings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }

    private void fetchStock(String startId) {
        if (currentStockQuery != null) {
            currentStockQuery.removeEventListener(currentStockQueryEventListener);
        }

        Query stockQuery = FirebaseDatabase.getInstance().getReference("pharmacies").child(currentPharmacy.getId()).child("stock").orderByValue();

        Log.d(TAG, MessageFormat.format("Fetching stocks for {0}", currentPharmacy.getId()));

        if (startId == null) {
            stockQuery.limitToLast(4);
        } else {
            stockQuery.startAt(startId).limitToLast(10);
        }

        currentStockQuery = stockQuery;

        currentStockQuery.addChildEventListener(currentStockQueryEventListener);
    }

    private void fetchRatings() {
        if (currentRatingsRef != null) {
            currentRatingsRef.removeEventListener(currentRatingsRefEventListener);
        }

        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("pharmacies").child(currentPharmacy.getId()).child("ratings");

        Log.d(TAG, MessageFormat.format("Fetching ratings for {0}", currentPharmacy.getId()));

        currentRatingsRef = ratingsRef;

        currentRatingsRef.addValueEventListener(currentRatingsRefEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateDetails() {
        ImageUtils.loadImage(fragmentContext, String.format("/pharmacies/%s", currentPharmacy.getId()), image);
        title.setText(currentPharmacy.getName());
        location.setText(Location.getAddress(currentPharmacy.getLocation(), fragmentContext));
        synchronized (medicineListAdapter) {
            medicines.clear();
            medicineListAdapter.notifyDataSetChanged();
        }
        fetchStock(null);
        fetchRatings();
    }

    public void update(String pharmacyId) {
        Log.d(TAG, MessageFormat.format("Fetching details for pharmacy {0}", pharmacyId));
        FirebaseDatabase.getInstance().getReference("pharmacies").child(pharmacyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Avoid loading stock data
                    Pharmacy pharmacy = new Pharmacy();
                    pharmacy.setId(pharmacyId);
                    pharmacy.setName(snapshot.child("name").getValue(String.class));
                    pharmacy.setLocation(snapshot.child("location").getValue(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location.class));
                    pharmacy.setStock(new HashMap<>());


                    Log.d(TAG, MessageFormat.format("Fetched details for pharmacy {0}", pharmacy.getId()));

                    if (pharmacy != null) {
                        currentPharmacy = pharmacy;
                        updateDetails();
                        updateUserActions();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, MessageFormat.format("Could not fetch pharmacy details: {0}", error.getMessage()));
            }
        });
    }


    // User related actions

    //  Favorites
    private void updateUserActions() {
        if (currentPharmacy == null) return;

        favouriteButton.setIcon(ContextCompat.getDrawable(fragmentContext, (favoritePharmacies.containsKey(currentPharmacy.getId())) ? R.drawable.favorite_fill : R.drawable.favorite_outline));
    }

    private void updateFavorite() {
        if (currentPharmacy == null) return;

        if (!favoritePharmacies.containsKey(currentPharmacy.getId())) {
            // Add to favorites
            AuthUtils.getUserRef().child("favoritePharmaciesIds").child(currentPharmacy.getId())
            .setValue(true)
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to add pharmacy to favorites:", task.getException());
                    Toast.makeText(fragmentContext, "Failed to add pharmacy to favorites", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Added pharmacy to favorites: " + currentPharmacy.getId());
                Toast.makeText(fragmentContext, "Pharmacy added to favorites", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Remove from favorites
            AuthUtils.getUserRef().child("favoritePharmaciesIds").child(currentPharmacy.getId())
            .removeValue()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to remove pharmacy from favorites:", task.getException());
                    Toast.makeText(fragmentContext, "Failed to remove pharmacy from favorites", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Removed pharmacy to favorites: " + currentPharmacy.getId());
                Toast.makeText(fragmentContext, "Pharmacy removed from favorites", Toast.LENGTH_SHORT).show();
            });
        }
    }

    //  Buys

    private void buyMedicine(Medicine medicine) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("pharmacies").child(currentPharmacy.getId()).child("stock").child(medicine.getId());
        db.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                String stockStr = mutableData.getValue(String.class);
                if (stockStr == null) {
                    return Transaction.success(mutableData); // No stock information
                }

                int stock = Integer.parseInt(stockStr);

                if (stock - 1 <= 0) {
                    // Insufficient stock
                    mutableData.setValue(null);
                    return Transaction.success(mutableData);
                }

                // Update stock
                mutableData.setValue(String.valueOf(stock - 1));
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("Map fragment getting medicines", "Database error: " + databaseError.getMessage());
                    Toast.makeText(fragmentContext, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (!committed) {
                    // Transaction was not committed, likely due to insufficient stock
                    Toast.makeText(fragmentContext, "Not enough stock!", Toast.LENGTH_SHORT).show();
                } else {
                    // Transaction committed and stock updated
                    Toast.makeText(fragmentContext, "1 dose of " + medicine.getName() + " bought successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //  Ratings

    private void submitReview(float rating) {
        if (rating == 0) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(fragmentContext, "You need to be logged in to submit a review", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        String pharmacyId = currentPharmacy.getId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference pharmacyRef = database.getReference("pharmacies").child(pharmacyId).child("ratings");
        DatabaseReference userReviewRef = database.getReference("users").child(userId).child("ratings").child(pharmacyId);
        userReviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userReviewSnapshot) {
                Float previousRating = userReviewSnapshot.getValue(Float.class);
                pharmacyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Long> ratings = (Map<String, Long>) dataSnapshot.getValue();
                        if (ratings == null) {
                            ratings = new HashMap<>();
                        }
                        // Adjust old rating if it exists and is different from current one
                        if (previousRating != null) {
                            if(previousRating == rating){
                                return;
                            }
                            String previousRatingKey = String.valueOf(previousRating.intValue());
                            ratings.put(previousRatingKey, ratings.getOrDefault(previousRatingKey, 0L) - 1);
                        }

                        // Update new rating
                        String ratingKey = String.valueOf((int) rating);
                        ratings.put(ratingKey, ratings.getOrDefault(ratingKey, 0L) + 1);

                        Map<String, Long> finalRatings = ratings;
                        pharmacyRef.setValue(ratings)
                                .addOnSuccessListener(aVoid -> {
                                    // Also update user's review history
                                    userReviewRef.setValue(rating)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(fragmentContext, "Review submitted", Toast.LENGTH_SHORT).show();
                                                updateProgressBars(finalRatings);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(fragmentContext, "Failed to submit review", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(fragmentContext, "Failed to submit review", Toast.LENGTH_SHORT).show();
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Failed to fetch current ratings data");
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Failed to fetch user review data");
            }
        });
    }

    private void updateProgressBars(Map<String, Long> ratingsCount) {
        int totalReviews = 0;
        for (Long count : ratingsCount.values()) {
            totalReviews += count;
        }
        if(totalReviews == 0){
            resetUserRatings();
            return;
        }

        int fiveStarCount = ratingsCount.getOrDefault("5", 0L).intValue();
        int fourStarCount = ratingsCount.getOrDefault("4", 0L).intValue();
        int threeStarCount = ratingsCount.getOrDefault("3", 0L).intValue();
        int twoStarCount = ratingsCount.getOrDefault("2", 0L).intValue();
        int oneStarCount = ratingsCount.getOrDefault("1", 0L).intValue();

        int fiveStarProgress =(int) ((fiveStarCount / (float) totalReviews) * 100);
        int fourStarProgress =(int) ((fourStarCount / (float) totalReviews) * 100);
        int threeStarProgress =(int) ((threeStarCount / (float) totalReviews) * 100);
        int twoStarProgress =(int) ((twoStarCount / (float) totalReviews) * 100);
        int oneStarProgress =(int) ((oneStarCount / (float) totalReviews) * 100);

        fiveStarProgressBar.setProgress(fiveStarProgress);
        fourStarProgressBar.setProgress(fourStarProgress);
        threeStarProgressBar.setProgress(threeStarProgress);
        twoStarProgressBar.setProgress(twoStarProgress);
        oneStarProgressBar.setProgress(oneStarProgress);

        float averageRating = calculateAverageRating(ratingsCount, totalReviews);
        averageRatingTextView.setText(String.format("%.1f", averageRating));
        userRatingBar.setRating(averageRating);
    }

    private float calculateAverageRating(Map<String, Long> ratingsCount, int totalReviews) {
        if (totalReviews == 0) {
            return 0;
        }

        int sum = 0;
        for (Map.Entry<String, Long> entry : ratingsCount.entrySet()) {
            int rating = Integer.parseInt(entry.getKey());
            sum += rating * entry.getValue().intValue();
        }

        return (float) sum / totalReviews;
    }

    private void resetUserRatings(){
        fiveStarProgressBar.setProgress(0);
        fourStarProgressBar.setProgress(0);
        threeStarProgressBar.setProgress(0);
        twoStarProgressBar.setProgress(0);
        oneStarProgressBar.setProgress(0);

        averageRatingTextView.setText("0");
        userRatingBar.setRating(0);
    }
}
