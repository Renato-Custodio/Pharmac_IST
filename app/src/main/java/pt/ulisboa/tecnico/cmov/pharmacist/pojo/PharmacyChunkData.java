package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

public class PharmacyChunkData {
    public String pharmacyId;

    public Location location;

    public boolean isSuspended;

    public PharmacyChunkData() {}

    public PharmacyChunkData(String pharmacyId, Location location, boolean isSuspended) {
        this.pharmacyId = pharmacyId;
        this.location = location;
        this.isSuspended = isSuspended;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }
}
