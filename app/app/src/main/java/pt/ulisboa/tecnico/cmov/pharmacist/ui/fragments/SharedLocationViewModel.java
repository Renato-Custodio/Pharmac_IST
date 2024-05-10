package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments;

import android.location.Location;
import androidx.lifecycle.ViewModel;

public class SharedLocationViewModel extends ViewModel {
    private Location location;

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}

