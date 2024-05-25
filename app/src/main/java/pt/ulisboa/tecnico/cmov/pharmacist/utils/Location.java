package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Location {
    public static String getAddress(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location location, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.lat,
                    location.lng,
                    1
            );
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressBuilder.append(", ");
                    }
                }
                return addressBuilder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Float getDistance(android.location.Location curruntLocation,
                                     pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location pharmacyLocation){
        android.location.Location location2 = new android.location.Location("");
        location2.setLatitude(pharmacyLocation.lat);
        location2.setLongitude(pharmacyLocation.lng);

        return curruntLocation.distanceTo(location2);
    }

    public static String getDistanceString(Double distance){
        double distanceInMeters = distance;
        double distanceInKilometers = distanceInMeters / 1000;
        String distanceText;
        if (distanceInKilometers < 1) {
            // If distance is less than 1 km, display in meters
            return Math.round(distanceInMeters) + " m";
        } else {
            // If distance is 1 km or more, display in kilometers
            return String.format("%.1f km", distanceInKilometers);
        }
    }

    public static String getDistanceString(Float distance) {
        return getDistanceString(Double.valueOf(distance));
    }
}
