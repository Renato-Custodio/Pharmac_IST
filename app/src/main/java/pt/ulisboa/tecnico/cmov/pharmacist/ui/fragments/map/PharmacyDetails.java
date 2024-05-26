package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesInPharmacyRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AdapterUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;


public class PharmacyDetails {
    private final TextView title, location, distance;
    private final ImageView image;
    private final MaterialButton favouriteButton;
    private Pharmacy currentPharmacy;
    private final Context fragmentContext;
    private RecyclerView.LayoutManager mLayoutManager;
    private final MedicinesInPharmacyRecyclerAdapter medicineListAdapter;
    private List<Medicine> medicines = new ArrayList<>();
    private Query currentStockQuery;
    private ChildEventListener currentStockQueryEventListener;
    private RecyclerView medicineList;
    private Map<String, Boolean> favoritePharmacies;

    private static final String TAG = PharmacyDetails.class.getSimpleName();

    public PharmacyDetails(Fragment fragment, View bottomSheetView, SharedLocationViewModel sharedLocationViewModel, MedicinesInPharmacyRecyclerAdapter.OnItemClickListener listener) {

        title = bottomSheetView.findViewById(R.id.details_pharmacy_title);
        location = bottomSheetView.findViewById(R.id.details_pharmacy_location);
        distance = bottomSheetView.findViewById(R.id.details_pharmacy_distance);
        image = bottomSheetView.findViewById(R.id.pharmacy_image);
        medicineList = bottomSheetView.findViewById(R.id.fragment_map_avaliable_medicines);
        favouriteButton = bottomSheetView.findViewById(R.id.favouriteButton);

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
        mLayoutManager = new LinearLayoutManager(fragment.getActivity());
        medicineListAdapter = new MedicinesInPharmacyRecyclerAdapter(medicines, fragment, currentPharmacy, listener, fragmentContext);
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
}
