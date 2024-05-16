package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MapChunk {
    @SerializedName("id")
    public String id;
    @SerializedName("location")
    public Location location;

    @SerializedName("chunkId")
    public String chunkId;

    @SerializedName("pharmacies")
    public List<Pharmacy> pharmacies;
}
