package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import pt.ulisboa.tecnico.cmov.pharmacist.QRCodeActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesInPharmacyRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;


public class PharmacyDetails {
    private final TextView title, location, distance;
    private final ImageView image;
    private Pharmacy currentPharmacy;
    private final Context fragmentContext;
    private RecyclerView.LayoutManager mLayoutManager;
    private final MedicinesInPharmacyRecyclerAdapter medicineListAdapter;
    private List<Medicine> medicines = new ArrayList<>();
    private Query currentStockQuery;
    private ChildEventListener currentStockQueryEventListener;
    private RecyclerView medicineList;

    private static final String TAG = PharmacyDetails.class.getName();

    public PharmacyDetails(Fragment fragment, View bottomSheetView, SharedLocationViewModel sharedLocationViewModel, MedicinesInPharmacyRecyclerAdapter.OnItemClickListener listener) {

        title = bottomSheetView.findViewById(R.id.details_pharmacy_title);
        location = bottomSheetView.findViewById(R.id.details_pharmacy_location);
        distance = bottomSheetView.findViewById(R.id.details_pharmacy_distance);
        image = bottomSheetView.findViewById(R.id.pharmacy_image);
        medicineList = bottomSheetView.findViewById(R.id.fragment_map_avaliable_medicines);

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

        currentStockQueryEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String medicineKey = snapshot.getKey().replace("Key_", "");

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
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String medicineId = snapshot.getKey();

                if (medicineId == null) return;

                OptionalInt index = IntStream.range(0, medicines.size())
                        .filter(i -> medicineId.equals(medicines.get(i).id))
                        .findFirst();

                if (!index.isPresent()) return;

                medicines.remove(index.getAsInt());
                medicineListAdapter.notifyItemRemoved(index.getAsInt());
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, MessageFormat.format("Could not fetch pharmacy details: {0}", error.getMessage()));
            }
        });
    }
}
