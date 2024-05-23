package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import java.util.Comparator;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;

public class PharmacyDistanceComparator implements Comparator<PharmacyDistance> {
    @Override
    public int compare(PharmacyDistance pharmacy1, PharmacyDistance pharmacy2) {
        // Compare based on the distance field
        return Double.compare(pharmacy1.distance, pharmacy2.distance);
    }
}
