package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyChunkData;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ChunkUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;


public class MarkersSystem {
    private final LruCache<String, DatabaseReference> chunksRefsCache;
    private final Map<String, MapChunk> chunksCache;
    private final Map<String, Marker> markers;
    private final ValueEventListener chunkListener;
    private final GoogleMap mapInstance;
    private final Context context;
    private static final String TAG = MarkersSystem.class.getSimpleName();
    private String currentClosestPharmacyId;
    private final Consumer<Marker> closestPharmacyCallBack;
    private Runnable onDismiss;
    private Map<String, Boolean> favoritePharmacies;
    private Marker currentSelectedMarker;

    public MarkersSystem(GoogleMap mapInstance, Context context, Consumer<Marker> closestPharmacyCallBack, Runnable onDismiss) {
        this.closestPharmacyCallBack = closestPharmacyCallBack;
        this.onDismiss = onDismiss;
        this.chunksRefsCache = new LruCache<>(20);
        this.mapInstance = mapInstance;
        this.context = context;
        this.chunksCache = new HashMap<>();
        this.markers = new HashMap<>();
        this.currentClosestPharmacyId = null;
        chunkListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MapChunk chunk = dataSnapshot.getValue(MapChunk.class);

                Log.d(TAG, "chunkListener triggered!!");

                if (chunk != null) {
                    Log.d(TAG, MessageFormat.format("Updating chunk: {0}", chunk.getChunkId()));
                    updateChunk(chunk);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.toException());
            }
        };

        AuthUtils.registerUserDataListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user == null) return;

                favoritePharmacies = user.getFavoritePharmaciesIds();
                updateMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isFavorite(Marker marker) {
        return favoritePharmacies.containsKey(Objects.requireNonNull(marker.getTag()).toString());
    }

    private void updateMarker(Marker marker) {
        Optional<String> currentSelectedId;

        if (currentSelectedMarker != null) {
            currentSelectedId = Optional.of(Objects.requireNonNull(currentSelectedMarker.getTag()).toString());
        } else {
            currentSelectedId = Optional.empty();
        }

        String pharmacyId = marker.getTag().toString();

        if (favoritePharmacies.containsKey(pharmacyId)) {
            // Is favorite
            if (currentSelectedId.isPresent()) {
                PharmacyMarker.setProps(marker, pharmacyId.equals(currentSelectedId.get()), true);
            } else {
                PharmacyMarker.setProps(marker, false, true);
            }
        } else {
            if (currentSelectedId.isPresent()) {
                PharmacyMarker.setProps(marker, pharmacyId.equals(currentSelectedId.get()), false);
            } else {
                PharmacyMarker.setProps(marker, false, false);
            }
        }
    }

    private void updateMarkers() {
        if (favoritePharmacies.isEmpty()) return;
        markers.values().forEach(this::updateMarker);
    }

    public void dismissSelection() {
        PharmacyMarker.setProps(currentSelectedMarker, false, isFavorite(currentSelectedMarker));
        currentSelectedMarker = null;
    }

    public void setActive(Marker marker, boolean active) {
        if (marker == null) return;

        if (active) {
            if (currentSelectedMarker != null) {
                if (!currentSelectedMarker.equals(marker)) {
                    dismissSelection();
                }
            }
            currentSelectedMarker = marker;
        }

        PharmacyMarker.setProps(marker, active, isFavorite(marker));
    }

    private void updateChunk(MapChunk chunk) {

        MapChunk oldChunk = chunksCache.get(chunk.getChunkId());

        if (oldChunk == null || oldChunk.chunkId == null) {
            Log.d(TAG, MessageFormat.format("{0} is a new chunk", chunk.getChunkId()));
            if(chunk.pharmacies == null)
                return;
            // New chunk, load all the markers
            chunk.pharmacies.forEach(pharmacyChunkData -> {
                Log.d(TAG, MessageFormat.format("   > Adding: {0}", pharmacyChunkData.pharmacyId));
                Marker marker = mapInstance.addMarker(PharmacyMarker.createNew(context, new LatLng(pharmacyChunkData.getLocation().lat, pharmacyChunkData.getLocation().lng)));
                markers.put(pharmacyChunkData.getPharmacyId(), marker);
                marker.setTag(pharmacyChunkData.pharmacyId);
                updateMarker(marker);
                ObjectAnimator.ofFloat(marker, "alpha", 0f, 1f).setDuration(500).start();
            });

        } else {
            Log.d(TAG, MessageFormat.format("{0} is an existing chunk", chunk.getChunkId()));

            Set<String> incomingPharmaciesIds = chunk.getPharmacies().stream().map(pharmacyChunkData -> pharmacyChunkData.pharmacyId).collect(Collectors.toSet());
            Set<PharmacyChunkData> pharmaciesToRemove = oldChunk.getPharmacies().stream().filter(pharmacyChunkData -> !incomingPharmaciesIds.contains(pharmacyChunkData.pharmacyId)).collect(Collectors.toSet());
            Set<PharmacyChunkData> pharmaciesToAdd = chunk.getPharmacies().stream().filter(pharmacyChunkData -> !markers.containsKey(pharmacyChunkData.pharmacyId)).collect(Collectors.toSet());

            pharmaciesToAdd.forEach(pharmacyChunkData -> {
                Log.d(TAG, MessageFormat.format("   > Adding: {0}", pharmacyChunkData.pharmacyId));
                Marker marker = mapInstance.addMarker(PharmacyMarker.createNew(context, new LatLng(pharmacyChunkData.getLocation().lat, pharmacyChunkData.getLocation().lng)));
                markers.put(pharmacyChunkData.getPharmacyId(), marker);
                marker.setTag(pharmacyChunkData.pharmacyId);
                updateMarker(marker);
                ObjectAnimator.ofFloat(marker, "alpha", 0f, 1f).setDuration(500).start();
            });

            pharmaciesToRemove.forEach(pharmacyChunkData -> {
                Log.d(TAG, MessageFormat.format("   > Removing: {0}", pharmacyChunkData.pharmacyId));
                markers.get(pharmacyChunkData.pharmacyId).remove();
                markers.remove(pharmacyChunkData.pharmacyId);
            });
        }

        chunksCache.put(chunk.getChunkId(), chunk);
    }

    private void checkCacheState() {
        Set<String> cachedChunkRefs = this.chunksRefsCache.snapshot().values().stream().map(DatabaseReference::getKey).collect(Collectors.toSet());

        Log.d(TAG, "Checking cache state...");

        for (String chunkId : chunksCache.keySet()) {
            if (!cachedChunkRefs.contains(chunkId)) {
                Log.d(TAG, MessageFormat.format("Chunk {0} was unloaded, removing markers...", chunkId));

                // Chunk was unloaded, remove ref and pharmacies markers
                if (chunksCache.containsKey(chunkId)) {
                    chunksCache.get(chunkId).pharmacies.forEach(pharmacyChunkData -> {
                        if (markers.containsKey(pharmacyChunkData.pharmacyId)) {
                            markers.get(pharmacyChunkData.pharmacyId).remove();
                            markers.remove(pharmacyChunkData.pharmacyId);
                        }
                    });
                }

                FirebaseDatabase.getInstance().getReference("chunks").child(chunkId).removeEventListener(chunkListener);
                chunksCache.remove(chunkId);
            }
        }
    }

    public void resetNearestDistance() {
        currentClosestPharmacyId = null;
    }

    public void findNearestPharmacy(String chunkId, android.location.Location currentLocation) {
        Log.d(TAG, "Checking distance...");

        PharmacyChunkData closestPharmacy = null;
        double minDistance = Float.MAX_VALUE;
        // Chunk was unloaded, remove ref and pharmacies markers
        if(chunksCache.get(chunkId) == null){
            return;
        }

        if (chunksCache.get(chunkId).pharmacies != null && !chunksCache.get(chunkId).pharmacies.isEmpty()) {
            for (PharmacyChunkData pharmacyChunkData : chunksCache.get(chunkId).pharmacies) {
                Float distance = Location.getDistance(currentLocation, pharmacyChunkData.location);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPharmacy = pharmacyChunkData;
                }
            }
        }

        if(closestPharmacy == null) return;


        if((Math.round(minDistance) <= 100 && currentClosestPharmacyId == null) || (Math.round(minDistance) <= 100 && !currentClosestPharmacyId.equals(closestPharmacy.pharmacyId))){
            this.closestPharmacyCallBack.accept(this.markers.get(closestPharmacy.pharmacyId));
            currentClosestPharmacyId = closestPharmacy.pharmacyId;
        } else {
            if (currentClosestPharmacyId != null) {
                LatLng current = markers.get(currentClosestPharmacyId).getPosition();
                Float distance = Location.getDistance(currentLocation, new pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location(current.latitude, current.longitude));
                if (Math.round(distance) > 100) {
                    onDismiss.run();
                    resetNearestDistance();
                }
            }
        }

    }

    public void update(LatLng coord) {
        String chunkId = ChunkUtils.getChunkId(coord);
        LatLng roundedCoord = new LatLng(ChunkUtils.precisionRound(coord.latitude, 100), ChunkUtils.precisionRound(coord.longitude, 100));

        // String selectedPharmacyId = (selectedMarker != null) ? (String) selectedMarker.getTag() : null;

        Log.d(TAG, MessageFormat.format("Requested chunk: {0} / {1}", roundedCoord, chunkId));

        if (this.chunksRefsCache.get(chunkId) == null) {
            Log.d(TAG, MessageFormat.format("Fetching chunk: {0} / {1} (Cached chunks refs: {2} / {3})", roundedCoord, chunkId, chunksRefsCache.size(), chunksRefsCache.maxSize()));

            DatabaseReference chunkRef = FirebaseDatabase.getInstance().getReference("chunks").child(chunkId);

            chunksRefsCache.put(chunkId, chunkRef);

            // Dummy chunk
            chunksCache.put(chunkId, new MapChunk());

            // Remove replaced chunks
            checkCacheState();

            chunkRef.addValueEventListener(chunkListener);
        }
    }

    public Marker getMarkerFromPharmacyId(String pharmacyId){
       return this.markers.get(pharmacyId);
    }
}
