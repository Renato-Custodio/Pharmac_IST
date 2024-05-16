package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.gson.annotations.SerializedName;
public class PharmacyDistance {

    public Pharmacy pharmacy;
    public double distance;

    public PharmacyDistance(Pharmacy pharmacy, double distance){
        this.distance = distance;
        this.pharmacy = pharmacy;
    }
}
