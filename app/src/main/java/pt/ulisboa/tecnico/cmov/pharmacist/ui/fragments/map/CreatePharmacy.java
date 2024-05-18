package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.gms.maps.model.LatLng;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;

public class CreatePharmacy extends Fragment {

    private LatLng latlng;

    private LatLng selectedLocation;
    private EditText nameEditText;
    private EditText addressEditText;
    private Button selectOnMapButton;
    private Button useCurrentLocationButton;
    private Button addPictureButton;
    private ImageView pictureImageView;
    private Button saveButton;

    public CreatePharmacy(){}

    public static CreatePharmacy newInstance(LatLng latLng) {
        CreatePharmacy fragment = new CreatePharmacy();
        fragment.latlng = latLng;
        fragment.selectedLocation = null;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_pharmacy, container, false);

        // Initialize views
        nameEditText = view.findViewById(R.id.nameEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        selectOnMapButton = view.findViewById(R.id.selectOnMapButton);
        useCurrentLocationButton = view.findViewById(R.id.useCurrentLocationButton);
        addPictureButton = view.findViewById(R.id.addPictureButton);
        pictureImageView = view.findViewById(R.id.pictureImageView);
        saveButton = view.findViewById(R.id.saveButton);

        // Set up listeners for buttons (add your logic here)
        selectOnMapButton.setOnClickListener(v -> selectOnMap());
        useCurrentLocationButton.setOnClickListener(v -> useCurrentLocation());
        addPictureButton.setOnClickListener(v -> addPicture());
        saveButton.setOnClickListener(v -> savePharmacy());

        return view;
    }

    private void selectOnMap() {
        // Implement the logic for selecting a location on the map
        this.selectedLocation = this.latlng;
        Toast.makeText(getActivity(), "Select on Map clicked", Toast.LENGTH_SHORT).show();
    }

    private void useCurrentLocation() {
        // Implement the logic for using the current location
        SharedLocationViewModel sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        Location location = sharedLocationViewModel.getLocation().getValue();
        this.selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Toast.makeText(getActivity(), "Use Current Location clicked", Toast.LENGTH_SHORT).show();
    }

    private void addPicture() {
        // Implement the logic for adding a picture
        Toast.makeText(getActivity(), "Add Picture clicked", Toast.LENGTH_SHORT).show();
    }

    private void savePharmacy() {
        // Read the inputs
        String pharmacyName = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(pharmacyName)) {
            nameEditText.setError("Pharmacy name is required");
            return;
        }

        if (this.selectedLocation == null) {
            Toast.makeText(getActivity(), "Please select a location method", Toast.LENGTH_SHORT).show();
            return;
        }

        // Implement the logic to save the pharmacy details
        Toast.makeText(getActivity(), "Pharmacy saved: " + pharmacyName + ", " + address, Toast.LENGTH_SHORT).show();
    }
}
