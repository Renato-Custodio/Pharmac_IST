package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Location;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.PharmacyDistance;

public class ClosestPharmaciesRecyclerAdapter extends RecyclerView.Adapter<ClosestPharmaciesRecyclerAdapter.ViewHolder> {
    private static List<PharmacyDistance> localDataSet = null;

    private Context context;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public ClosestPharmaciesRecyclerAdapter(List<PharmacyDistance> dataSet, Context context) {
        localDataSet = dataSet;
        this.context = context;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;

        private final TextView distance;

        private final TextView address;
        private final View rootView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            titleView = (TextView) view.findViewById(R.id.pharmacy_item_title);
            distance = (TextView) view.findViewById(R.id.pharmacy_item_distance);
            address = (TextView) view.findViewById(R.id.pharmacy_item_description);
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDistanceView() {
            return distance;
        }

        public TextView getAddressView() {
            return address;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.pharmacies_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d("ClosestPharmaciesRecyclerAdapter", MessageFormat.format("{0}",localDataSet));
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).pharmacy.name);
        viewHolder.getDistanceView().setText(String.valueOf(Math.round(localDataSet.get(position).distance)));
        viewHolder.getAddressView().setText(getAddressFromLocation(localDataSet.get(position).pharmacy.location));

    }

    private String getAddressFromLocation(Location location){
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}