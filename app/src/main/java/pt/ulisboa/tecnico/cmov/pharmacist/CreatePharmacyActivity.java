package pt.ulisboa.tecnico.cmov.pharmacist;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.search.SearchBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyChunkData;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ChunkUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageResultLaunchers;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class CreatePharmacyActivity extends AppCompatActivity {
    private LatLng latlng;

    private LatLng selectedLatLng;

    private EditText nameEditText;
    private TextView addressEditText;
    private Bitmap pharmacyPhoto = null;
    private Location location;
    private static final String TAG = CreatePharmacyActivity.class.getSimpleName();

    public Map<String, Boolean> ownedPharmaciesIds;

    ImageResultLaunchers imageResultLaunchers;

    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pharmacy);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedLatLng = (LatLng) extras.get("selected_coordinates");
            location = (Location) extras.get("current_coordinates");
            latlng = selectedLatLng;
            //The key argument here must match that used in the other activity
        }

        // Get UI Elements
        nameEditText = findViewById(R.id.pharmacy_name_text);
        addressEditText = ( (SearchBar) findViewById(R.id.pharmacy_address_search_bar)).getTextView();
        Button selectOnMapButton = findViewById(R.id.selectOnMapButton);
        Button useCurrentLocationButton = findViewById(R.id.useCurrentLocationButton);
        Button addPictureButton = findViewById(R.id.pharmacy_upload_photo_button);
        Button saveButton = findViewById(R.id.saveButton);

        // Init back button
        findViewById(R.id.create_pharmacy_back_button).setOnClickListener(e -> finish());

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
        imageResultLaunchers = ImageUtils.registerResultLaunchers(this, bitmap -> {
            ( (ImageView) findViewById(R.id.picture_pharmacy)).setImageBitmap(bitmap);
            pharmacyPhoto = bitmap;
        });

        addPictureButton.setOnClickListener(v -> {
            try {
                ImageUtils.openDialog(this, imageResultLaunchers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        saveButton.setOnClickListener(v -> {
            if(isSaving) return;
            isSaving = true;
            savePharmacy();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this, imageResultLaunchers);
    }

    private void selectOnMap() {
        Location location = new Location(""); // Set an empty string as provider

        location.setLatitude(selectedLatLng.latitude);
        location.setLongitude(selectedLatLng.longitude);
        String address = getAddressFromLocation(location);
        addressEditText.setText(address);

        latlng = selectedLatLng;

        Toast.makeText(this, "Using location selected in the map", Toast.LENGTH_SHORT).show();
    }

    private void useCurrentLocation() {


        if (location != null) {
            latlng = new LatLng(location.getLatitude(), location.getLongitude());
            String address = getAddressFromLocation(location);
            addressEditText.setText(address);
            Toast.makeText(this, "Current location used", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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
            Toast.makeText(this, "Please select a picture", Toast.LENGTH_SHORT).show();
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
                    ImageUtils.uploadImage(this, pharmacyPhoto, "pharmacies", newPharmacyRef.getKey(), metadata -> {
                        updateMapChunk(newPharmacyRef.getKey(), latlng.latitude, latlng.longitude);
                        updateOwnedPharmacies(newPharmacyRef.getKey());
                    });
                })
                .addOnFailureListener(e -> {
                    // Failed to add
                    Log.e("Firebase", "Failed to add pharmacy entry", e);
                    Toast.makeText(this, "ERROR: Pharmacy did not save: " + pharmacyName + ", " + address, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOwnedPharmacies(String pharmacyId){
        // Add to ownedPharmacies
        AuthUtils.getUserRef().child("ownedPharmaciesIds").child(pharmacyId)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to add pharmacy to ownedPharmacies:", task.getException());
                        Toast.makeText(this, "Failed to add pharmacy to ownedPharmacies", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "Added pharmacy to ownedPharmacies: " + pharmacyId);
                    Toast.makeText(this, "Pharmacy added to ownedPharmacies", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(), "Pharmacy saved", Toast.LENGTH_SHORT).show();
                            finish();
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