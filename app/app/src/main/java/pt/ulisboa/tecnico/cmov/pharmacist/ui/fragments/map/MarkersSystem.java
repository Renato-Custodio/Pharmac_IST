package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.client.APIFactory;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Pharmacy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarkersSystem {
    private final LruCache<String, MapChunk> mapCache;
    private final List<Marker> markers;

    private final GoogleMap mapInstance;

    private final Context context;

    public MarkersSystem(GoogleMap mapInstance, Context context) {
        this.mapCache = new LruCache<>(10);
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

    public void refreshPoints(Pharmacy selectedPharmacy, Consumer<Marker> callback) {
        if (!this.markers.isEmpty()) {
            this.markers.forEach(Marker::remove);
            this.markers.clear();
        }

        this.mapCache.snapshot().values().forEach((chunk) -> {
            chunk.pharmacies.forEach((pharmacy) -> {
                Marker marker = this.mapInstance.addMarker(PharmacyMarker.createNew(context, new LatLng(pharmacy.location.lat, pharmacy.location.lng)));

                if (marker != null) {
                    this.markers.add(marker);
                    marker.setTag(pharmacy);

                    if (selectedPharmacy != null) {
                        if (selectedPharmacy.id.equals(pharmacy.id)) {
                            callback.accept(marker);
                        }
                    }
                }
            });
        });
    }

    public void update(LatLng coord, Marker selectedMarker, Consumer<Marker> callback) {
        // Do we have this marker in the cache?
        LatLng roundedCoord = new LatLng(roundDecimal(coord.latitude, 100), roundDecimal(coord.longitude, 100));
        String chunkId = this.getChunkId(roundedCoord);

        Pharmacy selectedPharmacy = (selectedMarker != null) ? (Pharmacy) selectedMarker.getTag() : null;

        Log.d("MarkersSystem", MessageFormat.format("Requested chunk: {0} / {1}", roundedCoord, chunkId));

        if (this.mapCache.get(chunkId) == null) {
            Log.d("MarkersSystem", MessageFormat.format("Fetching chunk: {0} / {1} (Cached chunks: {2} / {3})", roundedCoord, chunkId, mapCache.size(), mapCache.maxSize()));

            Call<List<MapChunk>> chunks = APIFactory.getInterface().doGetMapFragment(roundedCoord.latitude, roundedCoord.longitude);
            chunks.enqueue(new Callback<List<MapChunk>>() {
                @Override
                public void onResponse(Call<List<MapChunk>> call, Response<List<MapChunk>> response) {
                    response.body().forEach((chunk) -> {
                        mapCache.put(chunk.chunkId, chunk);
                    });
                    refreshPoints(selectedPharmacy, callback);
                }

                @Override
                public void onFailure(Call<List<MapChunk>> call, Throwable t) {
                    Log.e("MarkersSystem", t.getMessage());
                    call.cancel();
                }
            });
        }

    }
}
