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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
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

    private Location lastKnownLocation;

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


    View bottomSheetView;
    TextView textViewTitle;
    TextView textViewLocation;
    TextView textViewDistance;

    Pharmacy pharmacy;

    // Fragment Lifecycle functions

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
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

        bottomSheetView = getView().findViewById(R.id.pharmacy_details);
        textViewTitle = bottomSheetView.findViewById(R.id.textView5);
        textViewLocation = bottomSheetView.findViewById(R.id.textView6);
        textViewDistance = bottomSheetView.findViewById(R.id.pharmacy_distance);
        sharedLocationViewModel.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if(pharmacy != null) {
                    textViewDistance.setText(getDistance(location, pharmacy));
                }
            }
        });
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

        mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Add a marker at the long-pressed location
                System.out.println("long press with lat: "+ latLng.latitude+" lng: "+latLng.longitude);
                // Replace the fragment
                CreatePharmacy newFragment = CreatePharmacy.newInstance(latLng);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainerView, newFragment);
                transaction.addToBackStack(null); // Add to back stack to allow back navigation
                transaction.commit();
            }
        });

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
                    Location location = locationResult.getLastLocation();
                    sharedLocationViewModel.setLocation(location);
                    onLocationChanged(location);
                }
            }
        }, null);
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

    private String getDistance(Location location, Pharmacy pharmacy){
        Float distance = pt.ulisboa.tecnico.cmov.pharmacist.utils.Location.getDistance(
                location ,pharmacy.location);
        return pt.ulisboa.tecnico.cmov.pharmacist.utils.Location.getDistanceString(
                Double.valueOf(distance));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("MapFragment:onMarkerClick", MessageFormat.format("Selected: {0}", marker.getPosition()));
        focus = MapFocus.MARKER;
        pharmacy = (Pharmacy) marker.getTag();
        onLocationChanged(marker.getPosition());
        unfocused();

        if (currentSelectedMarker != null) {
            PharmacyMarker.setActive(currentSelectedMarker, false);
        }


        if (pharmacy == null) {
            Log.e("MapFragment:onMarkerClick", "Pharmacy is null!");
            return false;
        }

        Picasso.get().load(MessageFormat.format("{0}/images/{1}", BuildConfig.SERVER_BASE_URL, pharmacy.picture)).into((ImageView) bottomSheetView.findViewById(R.id.pharmacy_image));
        textViewTitle.setText(pharmacy.name);
        pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location location =
                new pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location();
        location.lat = marker.getPosition().latitude;
        location.lng = marker.getPosition().longitude;
        textViewLocation.setText(
                pt.ulisboa.tecnico.cmov.pharmacist.utils.Location.getAddress(location, getContext()));

        textViewDistance.setText(getDistance(sharedLocationViewModel.getLocation().getValue() , pharmacy));
        currentSelectedMarker = marker;

        PharmacyMarker.setActive(marker, true);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        return true;
    }
}