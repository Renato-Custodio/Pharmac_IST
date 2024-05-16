package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedLocationViewModel extends ViewModel {
    private MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

    public void setLocation(Location location) {
        locationLiveData.setValue(location);
    }

    public LiveData<Location> getLocation() {
        return locationLiveData;
    }
}

