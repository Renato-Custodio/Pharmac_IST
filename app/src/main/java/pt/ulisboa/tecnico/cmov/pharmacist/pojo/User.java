package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@IgnoreExtraProperties
public class User {
    public String username;
    public Map<String, Boolean> favoritePharmaciesIds;
    public Map<String, Boolean> medicineNotificationIds;
    public Map<String, Boolean> ownedPharmaciesIds;
    public boolean isPharmacyOwner = false;

    public User() {
    }

    public User(String username, Map<String, Boolean> ownedPharmaciesIds, Map<String, Boolean> favoritePharmaciesIds, boolean isPharmacyOwner) {
        this.username = username;
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


    public void setOwnedPharmaciesIds(Map<String, Boolean> ownedPharmaciesIds) {
        this.ownedPharmaciesIds = ownedPharmaciesIds;
    }

    public Map<String, Boolean> getOwnedPharmaciesIds() {
        return ownedPharmaciesIds;
    }

    public void setFavoritePharmaciesIds(Map<String, Boolean> favoritePharmaciesIds) {
        this.favoritePharmaciesIds = favoritePharmaciesIds;
    }

    public void setPharmacyOwner(boolean pharmacyOwner) {
        isPharmacyOwner = pharmacyOwner;
    }
}
