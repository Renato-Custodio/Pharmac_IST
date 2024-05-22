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

    public User() {
    }

    public User(String username, String photoUrl, List<String> ownedPharmaciesIds, List<String> favoritePharmaciesIds, boolean isPharmacyOwner) {
        this.username = username;
        this.photoUrl = photoUrl;
        this.ownedPharmaciesIds = ownedPharmaciesIds;
        this.favoritePharmaciesIds = favoritePharmaciesIds;
        this.isPharmacyOwner = isPharmacyOwner;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setOwnedPharmaciesIds(List<String> ownedPharmaciesIds) {
        this.ownedPharmaciesIds = ownedPharmaciesIds;
    }

    public void setFavoritePharmaciesIds(List<String> favoritePharmaciesIds) {
        this.favoritePharmaciesIds = favoritePharmaciesIds;
    }

    public void setPharmacyOwner(boolean pharmacyOwner) {
        isPharmacyOwner = pharmacyOwner;
    }
}
