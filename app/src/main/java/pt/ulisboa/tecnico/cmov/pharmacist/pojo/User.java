package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;


@IgnoreExtraProperties
public class User {
    public String username;
    public String photoUrl;
    public List<String> ownedPharmaciesIds;
    public List<String> favoritePharmaciesIds;
    public boolean isPharmacyOwner = false;

    public User(String username, String photoUrl, List<String> ownedPharmaciesIds, List<String> favoritePharmaciesIds, boolean isPharmacyOwner) {
        this.username = username;
        this.photoUrl = photoUrl;
        this.ownedPharmaciesIds = ownedPharmaciesIds;
        this.favoritePharmaciesIds = favoritePharmaciesIds;
        this.isPharmacyOwner = isPharmacyOwner;
    }
}
