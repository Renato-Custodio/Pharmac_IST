package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@IgnoreExtraProperties
public class User {
    public String username;
    public String photoUrl;
    public List<String> ownedPharmaciesIds;
    public Map<String, Boolean> favoritePharmaciesIds;
    public Map<String, Boolean> medicineNotificationIds;
    public boolean isPharmacyOwner = false;

    public User() {
    }

    public User(String username, String photoUrl, List<String> ownedPharmaciesIds, Map<String, Boolean> favoritePharmaciesIds, boolean isPharmacyOwner) {
        this.username = username;
        this.photoUrl = photoUrl;
        this.ownedPharmaciesIds = ownedPharmaciesIds;
        this.favoritePharmaciesIds = favoritePharmaciesIds;
        this.isPharmacyOwner = isPharmacyOwner;
    }

    public Map<String, Boolean> getFavoritePharmaciesIds() {

        if (favoritePharmaciesIds == null) return new HashMap<>();

        return favoritePharmaciesIds;
    }

    public void setMedicineNotificationIds(Map<String, Boolean> medicineNotificationIds) {
        this.medicineNotificationIds = medicineNotificationIds;
    }

    public Map<String, Boolean> getMedicineNotificationIds() {

        if (medicineNotificationIds == null) return new HashMap<>();

        return medicineNotificationIds;
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

    public void setFavoritePharmaciesIds(Map<String, Boolean> favoritePharmaciesIds) {
        this.favoritePharmaciesIds = favoritePharmaciesIds;
    }

    public void setPharmacyOwner(boolean pharmacyOwner) {
        isPharmacyOwner = pharmacyOwner;
    }
}
