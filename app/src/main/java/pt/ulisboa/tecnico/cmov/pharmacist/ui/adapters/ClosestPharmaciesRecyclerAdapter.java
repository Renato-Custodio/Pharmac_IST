package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.MessageFormat;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

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
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d("ClosestPharmaciesRecyclerAdapter", MessageFormat.format("{0}",localDataSet));
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).pharmacy.name);


        viewHolder.getDistanceView().setText(Location.getDistanceString(localDataSet.get(position).distance));
        viewHolder.getAddressView().setText(Location.getAddress(localDataSet.get(position).pharmacy.location, context));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}