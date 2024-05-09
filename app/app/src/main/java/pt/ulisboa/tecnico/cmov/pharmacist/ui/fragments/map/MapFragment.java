package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.Manifest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.MessageFormat;

import pt.ulisboa.tecnico.cmov.pharmacist.R;


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
    private Location lastKnownLocation;

    private MarkersSystem markersSystem;

    // Fragment Lifecycle functions

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LOCATION, lastKnownLocation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

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
                onLocationChanged(lastKnownLocation);
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
            lastKnownLocation = null;
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
        lastKnownLocation = focus != MapFocus.CURRENT_POSITION ? lastKnownLocation : location;
        if (focus != MapFocus.DISABLED) mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
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

        currentSelectedMarker = marker;

        PharmacyMarker.setActive(marker, true);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        return true;
    }
}