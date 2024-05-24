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
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ChunkUtils;


public class MarkersSystem {
    private final LruCache<String, MapChunk> mapCache;
    private final List<Marker> markers;

    private final GoogleMap mapInstance;

    private final Context context;

    private static final String TAG = "MarkersSystem";

    public MarkersSystem(GoogleMap mapInstance, Context context) {
        this.mapCache = new LruCache<>(50);
        this.markers = new ArrayList<>();
        this.mapInstance = mapInstance;
        this.context = context;
    }

    public void refreshPoints(Set<String> newChunksIds, Pharmacy selectedPharmacy, Consumer<Marker> selectedMarkerCallback) {
        if (!this.markers.isEmpty()) {
            this.markers.forEach(Marker::remove);
            this.markers.clear();
        }

        this.mapCache.snapshot().values().forEach((chunk) -> {

            if (newChunksIds.contains(chunk.chunkId)) {
                Log.d(TAG, MessageFormat.format("New pharmacies (Chunk {0}): {1}", chunk.chunkId, chunk.pharmaciesIDs.toString()));
            }

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
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                    }
                });


            });
        });
    }

    public void update(LatLng coord, Marker selectedMarker, Consumer<Marker> callback) {
        String chunkId = ChunkUtils.getChunkId(coord);
        LatLng roundedCoord = new LatLng(ChunkUtils.precisionRound(coord.latitude, 100), ChunkUtils.precisionRound(coord.longitude, 100));

        Pharmacy selectedPharmacy = (selectedMarker != null) ? (Pharmacy) selectedMarker.getTag() : null;

        Log.d(TAG, MessageFormat.format("Requested chunk: {0} / {1}", roundedCoord, chunkId));

        if (this.mapCache.get(chunkId) == null) {
            Log.d(TAG, MessageFormat.format("Fetching chunk: {0} / {1} (Cached chunks: {2} / {3})", roundedCoord, chunkId, mapCache.size(), mapCache.maxSize()));

            FirebaseDatabase database = FirebaseDatabase.getInstance();

            database.getReference().child("chunks").child(chunkId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Set<String> loadedChunkIds = new HashSet<>();
                        MapChunk chunk = dataSnapshot.getValue(MapChunk.class);

                        mapCache.put(chunk.chunkId, chunk);
                        loadedChunkIds.add(chunk.chunkId);

                        Log.d(TAG, MessageFormat.format("Loaded chunks: {0}", loadedChunkIds));
                        refreshPoints(loadedChunkIds, selectedPharmacy, callback);
                    } else {
                        Log.w(TAG, MessageFormat.format("DataSnapshot: {0}", dataSnapshot.getChildren()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                }
            });
        }

    }
}
