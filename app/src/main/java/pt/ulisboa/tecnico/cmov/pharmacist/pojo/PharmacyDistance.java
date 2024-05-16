package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.gson.annotations.SerializedName;
public class PharmacyDistance {
    @SerializedName("pharmacy")
    public Pharmacy pharmacy;
    @SerializedName("distance")
    public double distance;
}
