package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.QRCodeActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
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
    private MedicinesInPharmacyRecyclerAdapter medicineListAdapter;

    public PharmacyDetails(Fragment fragment, View bottomSheetView, SharedLocationViewModel sharedLocationViewModel) {

        title = bottomSheetView.findViewById(R.id.details_pharmacy_title);
        location = bottomSheetView.findViewById(R.id.details_pharmacy_location);
        distance = bottomSheetView.findViewById(R.id.details_pharmacy_distance);
        image = bottomSheetView.findViewById(R.id.pharmacy_image);

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

    }

    private void updateDetails() {
        ImageUtils.loadImage(fragmentContext,String.format("/pharmacies/%s", currentPharmacy.getId()), image);
        title.setText(currentPharmacy.getName());
        location.setText(Location.getAddress(currentPharmacy.getLocation(), fragmentContext));
    }

    public void update(String pharmacyId) {
        FirebaseDatabase.getInstance().getReference("pharmacies").child(pharmacyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                    if (pharmacy != null) {
                        currentPharmacy = pharmacy;
                        updateDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
