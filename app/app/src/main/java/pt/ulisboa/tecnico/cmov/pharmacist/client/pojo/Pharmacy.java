package pt.ulisboa.tecnico.cmov.pharmacist.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Pharmacy {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("location")
    public Location location;
}
