package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.search.SearchBar;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyChunkData;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ChunkUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageResultLaunchers;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;


public class CreatePharmacy extends Fragment {
    private LatLng latlng;
    private EditText nameEditText;
    private TextView addressEditText;
    private Bitmap pharmacyPhoto = null;

    private static final String TAG = CreatePharmacy.class.getSimpleName();

    public CreatePharmacy() {}

    public static CreatePharmacy newInstance(LatLng latLng) {
        CreatePharmacy fragment = new CreatePharmacy();
        fragment.latlng = latLng;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pharmacy, container, false);

        // Get UI Elements
        nameEditText = view.findViewById(R.id.pharmacy_name_text);
        addressEditText = ( (SearchBar) view.findViewById(R.id.pharmacy_address_search_bar)).getTextView();
        Button selectOnMapButton = view.findViewById(R.id.selectOnMapButton);
        Button useCurrentLocationButton = view.findViewById(R.id.useCurrentLocationButton);
        Button addPictureButton = view.findViewById(R.id.pharmacy_upload_photo_button);
        Button saveButton = view.findViewById(R.id.saveButton);

        // Init back button
        view.findViewById(R.id.create_pharmacy_back_button).setOnClickListener(e ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Parse address of location
        if (latlng != null){
            Location location = new Location(""); // Set an empty string as provider

            location.setLatitude(latlng.latitude);
            location.setLongitude(latlng.longitude);
            String address = getAddressFromLocation(location);
            addressEditText.setText(address);
        }

        // Location buttons callbacks
        selectOnMapButton.setOnClickListener(v -> selectOnMap());
        useCurrentLocationButton.setOnClickListener(v -> useCurrentLocation());

        // Image dialogs
        ImageResultLaunchers imageResultLaunchers = ImageUtils.registerResultLaunchers(this, bitmap -> {
            ( (ImageView) view.findViewById(R.id.picture_pharmacy)).setImageBitmap(bitmap);
            pharmacyPhoto = bitmap;
        });

        addPictureButton.setOnClickListener(v -> {
            try {
                ImageUtils.openDialog(getActivity(), imageResultLaunchers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        saveButton.setOnClickListener(v -> savePharmacy());

        return view;
    }

    private void selectOnMap() {
        Toast.makeText(getActivity(), "Select on Map clicked", Toast.LENGTH_SHORT).show();
    }

    private void useCurrentLocation() {
        SharedLocationViewModel sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        Location location = sharedLocationViewModel.getLocation().getValue();
        if (location != null) {
            String address = getAddressFromLocation(location);
            addressEditText.setText(address);
            Toast.makeText(getActivity(), "Current location used", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Address";
    }

    private void savePharmacy() {
        String pharmacyName = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference pharmacyRef = database.getReference("pharmacies");

        if (pharmacyPhoto == null) {
            Toast.makeText(getActivity(), "Please select a picture", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            addressEditText.setError("Pharmacy location is required");
            return;
        }

        if (TextUtils.isEmpty(pharmacyName)) {
            nameEditText.setError("Pharmacy name is required");
            return;
        }

        // Add the new entry with a unique key
        DatabaseReference newPharmacyRef = pharmacyRef.push();

        // Create new pharmacy
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(newPharmacyRef.getKey());
        pharmacy.setName(pharmacyName);
        pharmacy.setLocation(new pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location(latlng.latitude, latlng.longitude));

        // Add the new entry to the database
        newPharmacyRef.setValue(pharmacy)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pharmacy entry added successfully with key: " + newPharmacyRef.getKey());
                    ImageUtils.uploadImage(getActivity(), pharmacyPhoto, "pharmacies", newPharmacyRef.getKey(), metadata -> {
                        updateMapChunk(newPharmacyRef.getKey(), latlng.latitude, latlng.longitude);
                    });
                })
                .addOnFailureListener(e -> {
                    // Failed to add
                    Log.e("Firebase", "Failed to add pharmacy entry", e);
                    Toast.makeText(getActivity(), "ERROR: Pharmacy did not save: " + pharmacyName + ", " + address, Toast.LENGTH_SHORT).show();
                });
    }
    private void updateMapChunk(String pharmacyId, double latitude, double longitude) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference chunksRef = database.getReference("chunks");

        String chunkId = ChunkUtils.getChunkId(latitude, longitude);
        chunksRef.child(chunkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MapChunk mapChunk;

                PharmacyChunkData pharmacyChunkData = new PharmacyChunkData(pharmacyId, new pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location(latitude, longitude));

                if (snapshot.exists()) {
                    mapChunk = snapshot.getValue(MapChunk.class);

                    if (mapChunk != null) {
                        if (mapChunk.pharmacies == null) {
                            mapChunk.pharmacies = new ArrayList<>();
                        }

                        mapChunk.addPharmacy(pharmacyChunkData);
                    }
                } else {
                    mapChunk = new MapChunk(new pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location(ChunkUtils.precisionRound(latlng.latitude, 100), ChunkUtils.precisionRound(latlng.longitude, 100)), chunkId);
                    mapChunk.setPharmacies(Collections.singletonList(pharmacyChunkData));
                }

                chunksRef.child(chunkId).setValue(mapChunk)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, MessageFormat.format("MapChunk {0} updated successfully", chunkId));
                            Toast.makeText(getActivity(), "Pharmacy saved", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update MapChunk", e));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read MapChunk", error.toException());
            }
        });
    }
}