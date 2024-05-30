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
    public Map<String, Boolean> flaggedPharmaciesIds;
    public boolean isPharmacyOwner = false;
    public boolean isBanned = false;

    public User() {
    }

    public User(String username, Map<String, Boolean> ownedPharmaciesIds, Map<String, Boolean> favoritePharmaciesIds, Map<String, Boolean> flaggedPharmaciesIds, boolean isPharmacyOwner, boolean isBanned) {
        this.username = username;
        this.ownedPharmaciesIds = ownedPharmaciesIds;
        this.favoritePharmaciesIds = favoritePharmaciesIds;
        this.flaggedPharmaciesIds = flaggedPharmaciesIds;
        this.isPharmacyOwner = isPharmacyOwner;
        this.isBanned = isBanned;
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

    public Map<String, Boolean> getFlaggedPharmaciesIds() { return this.flaggedPharmaciesIds; }

    public void setFlaggedPharmaciesIds(Map<String, Boolean> flaggedPharmaciesIds) {
        this.flaggedPharmaciesIds = flaggedPharmaciesIds;
    }
}
