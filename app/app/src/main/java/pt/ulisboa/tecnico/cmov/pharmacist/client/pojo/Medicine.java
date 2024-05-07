package pt.ulisboa.tecnico.cmov.pharmacist.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Medicine {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;
    @SerializedName("purpose")
    public String purpose;
    @SerializedName("picture")
    public String picture;
}
