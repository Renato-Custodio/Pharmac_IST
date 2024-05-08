package pt.ulisboa.tecnico.cmov.pharmacist.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("lat")
    public double lat;
    @SerializedName("lng")
    public double lng;
}
