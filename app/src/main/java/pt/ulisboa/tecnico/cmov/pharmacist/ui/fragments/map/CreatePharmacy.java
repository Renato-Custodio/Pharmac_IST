package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.search.SearchBar;
import com.google.android.material.transition.MaterialFadeThrough;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.MainActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;


public class CreatePharmacy extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private LatLng latlng;
    private LatLng selectedLocation;
    private EditText nameEditText;
    private TextView addressEditText;
    private Button selectOnMapButton;
    private Button useCurrentLocationButton;
    private Button addPictureButton;
    private ImageView pictureImageView;
    private Button saveButton;

    public CreatePharmacy() {}

    public static CreatePharmacy newInstance(LatLng latLng) {
        CreatePharmacy fragment = new CreatePharmacy();
        fragment.latlng = latLng;
        fragment.selectedLocation = null;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnterTransition(new MaterialFadeThrough());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pharmacy, container, false);

        nameEditText = view.findViewById(R.id.pharmacy_name_text);
        addressEditText = ( (SearchBar) view.findViewById(R.id.pharmacy_address_search_bar)).getTextView();

        view.findViewById(R.id.create_pharmacy_back_button).setOnClickListener(e ->
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack()
        );

        if (latlng != null){
            Location location = new Location(""); // Set an empty string as provider

            location.setLatitude(latlng.latitude);
            location.setLongitude(latlng.longitude);
            String address = getAddressFromLocation(location);
            addressEditText.setText(address);
        }
        selectOnMapButton = view.findViewById(R.id.selectOnMapButton);
        useCurrentLocationButton = view.findViewById(R.id.useCurrentLocationButton);
        // addPictureButton = view.findViewById(R.id.addPictureButton);
        // pictureImageView = view.findViewById(R.id.pictureImageView);
        saveButton = view.findViewById(R.id.saveButton);

        selectOnMapButton.setOnClickListener(v -> selectOnMap());
        useCurrentLocationButton.setOnClickListener(v -> useCurrentLocation());
        // addPictureButton.setOnClickListener(v -> showPictureDialog());
        saveButton.setOnClickListener(v -> savePharmacy());



        return view;
    }

    private void selectOnMap() {
        this.selectedLocation = this.latlng;
        Toast.makeText(getActivity(), "Select on Map clicked", Toast.LENGTH_SHORT).show();
    }

    private void useCurrentLocation() {
        SharedLocationViewModel sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        Location location = sharedLocationViewModel.getLocation().getValue();
        if (location != null) {
            this.selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
            String address = getAddressFromLocation(location);
            addressEditText.setText(address);
            Toast.makeText(getActivity(), "Current location used", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

        if (TextUtils.isEmpty(pharmacyName)) {
            nameEditText.setError("Pharmacy name is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(getActivity(), "Please input a location", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getActivity(), "Pharmacy saved: " + pharmacyName + ", " + address, Toast.LENGTH_SHORT).show();

        // Replace the fragment
        MapFragment newFragment = new MapFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerView, newFragment);
        transaction.addToBackStack(null); // Add to back stack to allow back navigation
        transaction.commit();
    }
}