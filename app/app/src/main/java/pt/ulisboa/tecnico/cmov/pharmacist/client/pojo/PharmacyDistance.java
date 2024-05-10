package pt.ulisboa.tecnico.cmov.pharmacist.client.pojo;

import com.google.gson.annotations.SerializedName;
public class PharmacyDistance {
    @SerializedName("pharmacy")
    public Pharmacy pharmacy;
    @SerializedName("distance")
    public double distance;
}
