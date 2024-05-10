package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.Manifest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.PlacesAutoCompleteAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;


enum MapFocus {
    DISABLED,
    CURRENT_POSITION,
    MARKER
}

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener {

    private static final Float DEFAULT_ZOOM = 18f;
    private GoogleMap mapInstance;
    private final LatLng defaultLocationCoords = new LatLng(20.7580154, 72.1113358);

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    // UI
    private MapFocus focus = MapFocus.CURRENT_POSITION;
    private FloatingActionButton focusButton;
    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;
    private BottomSheetBehavior bottomSheetBehavior;

    // Persistence
    private static final String KEY_LOCATION = "map_location";

    private Marker currentSelectedMarker;
    private SharedLocationViewModel sharedLocationViewModel;
    private MarkersSystem markersSystem;

    private RecyclerView addressResultsView;
    private SearchView searchView;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private SearchBar searchBar;

    // Fragment Lifecycle functions

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        if (savedInstanceState != null) {
            sharedLocationViewModel.setLocation(savedInstanceState.getParcelable(KEY_LOCATION));
        }
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LOCATION, sharedLocationViewModel.getLocation());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize zoom controls
        zoomIn = view.findViewById(R.id.zoom_in_action_button);
        zoomOut = view.findViewById(R.id.zoom_out_action_button);

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfocused();
                mapInstance.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfocused();
                mapInstance.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        // Initialize focus
        focusButton = view.findViewById(R.id.focus_action_button);
        focusButton.setVisibility(View.INVISIBLE);

        focusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                focus = MapFocus.CURRENT_POSITION;
                onLocationChanged(sharedLocationViewModel.getLocation());
                focusButton.setVisibility(View.INVISIBLE);
            }
        });

        // Initialize bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.pharmacy_details));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setFitToContents(true);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                if (state == BottomSheetBehavior.STATE_HIDDEN) {
                    PharmacyMarker.setActive(currentSelectedMarker, false);
                    currentSelectedMarker = null;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {}
        });

        // Initialize places search
        addressResultsView = view.findViewById(R.id.address_search_results);
        searchView = view.findViewById(R.id.address_search_view);
        searchBar = view.findViewById(R.id.address_search_bar);

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getContext());
        addressResultsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAutoCompleteAdapter.setClickListener(new PlacesAutoCompleteAdapter.ClickListener() {
            @Override
            public void click(Place place) {
                searchView.setText(place.getAddress());
                searchBar.setText(searchView.getText());
                searchView.hide();

                LatLng position = place.getLatLng();

                if (position != null) {
                    unfocused();
                    mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
                }
            }
        });

        addressResultsView.setAdapter(mAutoCompleteAdapter);
        mAutoCompleteAdapter.notifyDataSetChanged();

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                    if (addressResultsView.getVisibility() == View.GONE) {addressResultsView.setVisibility(View.VISIBLE);}
                } else {
                    if (addressResultsView.getVisibility() == View.VISIBLE) {addressResultsView.setVisibility(View.GONE);}
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    // Location Permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // PERMISSION GRANTED
                        Log.d("MapFragment", "Permission granted! Retrying...");
                        onMapReady(mapInstance);
                    } else {
                        Log.e("MapFragment", "Permission denied! Using default location");
                        mapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocationCoords, DEFAULT_ZOOM));
                    }
                }
            }
    );

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d("MapFragment", "Map is ready, checking location permissions...");

        mapInstance = googleMap;
        mapInstance.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext(), R.raw.map_style));

        markersSystem = new MarkersSystem(mapInstance, getContext());

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("MapFragment", "Location is not enabled, requesting permissions...");
            mapInstance.setMyLocationEnabled(false);
            sharedLocationViewModel.setLocation(null);
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        mapInstance.getUiSettings().setMyLocationButtonEnabled(false);
        mapInstance.setMyLocationEnabled(true);
        mapInstance.setOnMapLoadedCallback(this);
        mapInstance.setOnCameraMoveStartedListener(this);
        mapInstance.setOnCameraIdleListener(this);
        mapInstance.setOnMarkerClickListener(this);
    }

    // Location related functions
    @SuppressLint("MissingPermission")
    @Override
    public void onMapLoaded() {

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setWaitForAccurateLocation(false).setMinUpdateIntervalMillis(1000).setMaxUpdateDelayMillis(2000).build();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    onLocationChanged(locationResult.getLastLocation());
                }
            }
        }, null);

        // mapInstance.addMarker(PharmacyMarker.createNew(getContext(), new LatLng(38.7611, -9.1647)));
        // mapInstance.addMarker(PharmacyMarker.createNew(getContext(), new LatLng(38.7608, -9.1647)));
    }

    private void onLocationChanged(@NonNull Location location) {
        Location lastKnownLocation = sharedLocationViewModel.getLocation();
        lastKnownLocation = focus != MapFocus.CURRENT_POSITION ? lastKnownLocation : location;
        if (focus != MapFocus.DISABLED) mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
        sharedLocationViewModel.setLocation(lastKnownLocation);
    }

    private void onLocationChanged(@NonNull LatLng coordinates) {
        Location location = new Location("coordinates");
        location.setLatitude(coordinates.latitude);
        location.setLongitude(coordinates.longitude);
        this.onLocationChanged(location);
    }

    // Focus system
    private void unfocused() {
        focus = MapFocus.DISABLED;
        focusButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            unfocused();
        }
    }

    // Markers system
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCameraIdle() {
        LatLng coordinates = mapInstance.getCameraPosition().target;
        markersSystem.update(coordinates, currentSelectedMarker, (marker) -> {
            Log.d("MapFragment", "Replaced current marker");
            currentSelectedMarker = marker;
            PharmacyMarker.setActive(marker, true);
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("MapFragment", MessageFormat.format("Selected: {0}", marker.getPosition()));
        focus = MapFocus.MARKER;
        onLocationChanged(marker.getPosition());
        unfocused();

        if (currentSelectedMarker != null) {
            PharmacyMarker.setActive(currentSelectedMarker, false);
        }
        Pharmacy pharmacy = (Pharmacy) marker.getTag();
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());;


        View bottomSheetView = getView().findViewById(R.id.pharmacy_details);
        TextView textViewTitle = bottomSheetView.findViewById(R.id.textView5);
        TextView textViewLocation = bottomSheetView.findViewById(R.id.textView6);
        if (textViewTitle != null && pharmacy != null) {
            textViewTitle.setText(pharmacy.name);
            // Get address from coordinates using Geocoder
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        marker.getPosition().latitude,
                        marker.getPosition().longitude,
                        1
                );
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    StringBuilder addressBuilder = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressBuilder.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) {
                            addressBuilder.append(", ");
                        }
                    }
                    textViewLocation.setText(addressBuilder.toString());
                } else {
                    textViewLocation.setText("Address not found");
                }
            } catch (IOException e) {
                e.printStackTrace();
                textViewLocation.setText("Address not available");
            }
        }
        currentSelectedMarker = marker;

        PharmacyMarker.setActive(marker, true);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        return true;
    }
}