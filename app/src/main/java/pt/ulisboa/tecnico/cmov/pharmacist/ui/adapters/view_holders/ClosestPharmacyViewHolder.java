package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.BindViewDataSetter;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

public class ClosestPharmacyViewHolder extends RecyclerView.ViewHolder implements BindViewDataSetter {
    private final TextView titleView;
    private final TextView distance;
    private final TextView address;
    private final Context context;
    private final List<PharmacyDistance> localDataSet;

    public ClosestPharmacyViewHolder(View view, Context context, List<PharmacyDistance> localDataSet) {
        super(view);

        titleView = view.findViewById(R.id.pharmacy_item_title);
        distance = view.findViewById(R.id.pharmacy_item_distance);
        address = view.findViewById(R.id.pharmacy_item_description);
        this.localDataSet = localDataSet;
        this.context = context;
    }

    @Override
    public void setData(int position) {
        titleView.setText(localDataSet.get(position).pharmacy.getName());
        address.setText(Location.getAddress(localDataSet.get(position).pharmacy.getLocation(), context));
        distance.setText(Location.getDistanceString(localDataSet.get(position).distance));
    }
}
