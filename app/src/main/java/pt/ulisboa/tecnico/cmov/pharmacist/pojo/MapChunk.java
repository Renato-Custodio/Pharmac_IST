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
    public List<String> pharmaciesIDs;
    public MapChunk(){}


    public MapChunk(Location location, String chunkId){
        this.chunkId = chunkId;
        this.location = location;
        this.pharmaciesIDs = new ArrayList<>();
    }

    public void addPharmacyId(String pharmacyId){
        this.pharmaciesIDs.add(pharmacyId);
    }

    public List<String> getPharmaciesIDs() {
        return pharmaciesIDs;
    }

    public void setPharmaciesIDs(List<String> pharmaciesIDs) {
        this.pharmaciesIDs = pharmaciesIDs;
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


    public static double precisionRound(double a, int dec) {
        return (double) Math.round(a * dec) / dec;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getMapChunk(double lat, double lng) {
       return Base64.getEncoder().encodeToString(MessageFormat.format("{0}{1}", lat, lng).getBytes());
    }
}
