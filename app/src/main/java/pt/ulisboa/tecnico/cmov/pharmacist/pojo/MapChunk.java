package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MapChunk {
    public String id;
    public Location location;
    public String chunkId;
    public List<Pharmacy> pharmacies;
}
