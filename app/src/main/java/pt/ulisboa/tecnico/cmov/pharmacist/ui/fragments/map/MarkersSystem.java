package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;


public class MarkersSystem {
    private final LruCache<String, MapChunk> mapCache;
    private final List<Marker> markers;

    private final GoogleMap mapInstance;

    private final Context context;

    public MarkersSystem(GoogleMap mapInstance, Context context) {
        this.mapCache = new LruCache<>(50);
        this.markers = new ArrayList<>();
        this.mapInstance = mapInstance;
        this.context = context;
    }

    public double roundDecimal(double a, int dec) {
        return (double) Math.round(a * dec) / dec;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getChunkId(LatLng coord) {
        return Base64.getEncoder().encodeToString(MessageFormat.format("{0}{1}", coord.latitude, coord.longitude).getBytes());
    }

    public void refreshPoints(Set<String> newChunksIds, Pharmacy selectedPharmacy, Consumer<Marker> selectedMarkerCallback) {
        if (!this.markers.isEmpty()) {
            this.markers.forEach(Marker::remove);
            this.markers.clear();
        }

        this.mapCache.snapshot().values().forEach((chunk) -> {
            chunk.pharmaciesIDs.forEach((pharmacyId) -> {
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                Query pharmacyRef = database.getReference("pharmacies").orderByChild("id").equalTo(pharmacyId);
                pharmacyRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Pharmacy pharmacy = null;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            pharmacy = snapshot.getValue(Pharmacy.class);
                        }
                        Marker marker = mapInstance.addMarker(PharmacyMarker.createNew(context,
                                new LatLng(pharmacy.getLocation().lat, pharmacy.getLocation().lng)));

                        if (marker != null) {
                            markers.add(marker);
                            marker.setTag(pharmacy);

                            if (newChunksIds.contains(chunk.chunkId)) {
                                ObjectAnimator.ofFloat(marker, "alpha", 0f, 1f).setDuration(500).start();
                            }

                            if (selectedPharmacy != null) {
                                if (selectedPharmacy.getId().equals(pharmacy.getId())) {
                                    // Persist selected marker after points refresh
                                    selectedMarkerCallback.accept(marker);
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MarkerSystem getting chuncks", "Database error: " + databaseError.getMessage());
                    }
                });


            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void update(LatLng coord, Marker selectedMarker, Consumer<Marker> callback) {
        // Do we have this marker in the cache?
        LatLng roundedCoord = new LatLng(roundDecimal(coord.latitude, 100), roundDecimal(coord.longitude, 100));
        String chunkId = this.getChunkId(roundedCoord);

        Pharmacy selectedPharmacy = (selectedMarker != null) ? (Pharmacy) selectedMarker.getTag() : null;

        Log.d("MarkersSystem", MessageFormat.format("Requested chunk: {0} / {1}", roundedCoord, chunkId));

        if (this.mapCache.get(chunkId) == null) {
            Log.d("MarkersSystem", MessageFormat.format("Fetching chunk: {0} / {1} (Cached chunks: {2} / {3})", roundedCoord, chunkId, mapCache.size(), mapCache.maxSize()));

            FirebaseDatabase database = FirebaseDatabase.getInstance();

            database.getReference().child("chunks").child(chunkId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Set<String> loadedChunkIds = new HashSet<>();
                        MapChunk chunk = dataSnapshot.getValue(MapChunk.class);

                        mapCache.put(chunk.chunkId, chunk);
                        loadedChunkIds.add(chunk.chunkId);

                        Log.d("MarkersSystem", MessageFormat.format("Loaded chunks: {0}", loadedChunkIds));
                        refreshPoints(loadedChunkIds, selectedPharmacy, callback);
                    } else {
                        Log.w("MarkerSystem", MessageFormat.format("DataSnapshot: {0}", dataSnapshot.getChildren()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MarkerSystem", "Database error: " + databaseError.getMessage());
                }
            });
        }

    }
}
