package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.PharmacyDistance;

public class ClosestPharmaciesRecyclerAdapter extends RecyclerView.Adapter<ClosestPharmaciesRecyclerAdapter.ViewHolder> {
    private static List<PharmacyDistance> localDataSet = null;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public ClosestPharmaciesRecyclerAdapter(List<PharmacyDistance> dataSet) {
        localDataSet = dataSet;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;

        private final TextView distance;
        private final View rootView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            titleView = (TextView) view.findViewById(R.id.pharmacy_item_title);
            distance = (TextView) view.findViewById(R.id.pharmacy_item_distance);
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDistanceView() {
            return distance;
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
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}