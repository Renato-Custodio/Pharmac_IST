package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.SerializedName;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapChunk {
    public Location location;
    public String chunkId;
    public List<PharmacyChunkData> pharmacies;

    public MapChunk() {}

    public MapChunk(Location location, String chunkId){
        this.chunkId = chunkId;
        this.location = location;
        this.pharmacies = new ArrayList<>();
    }

    public void addPharmacy(PharmacyChunkData pharmacy){
        this.pharmacies.add(pharmacy);
    }

    public List<PharmacyChunkData> getPharmacies() {
        return pharmacies;
    }

    public void setPharmacies(List<PharmacyChunkData> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    @Override
    public String toString() {
        return "MapChunk{" +
                "location=" + location +
                ", chunkId='" + chunkId + '\'' +
                ", pharmacies=" + pharmacies +
                '}';
    }
}
