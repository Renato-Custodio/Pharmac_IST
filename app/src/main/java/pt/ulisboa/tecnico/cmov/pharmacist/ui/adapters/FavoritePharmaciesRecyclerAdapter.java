package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

public class FavoritePharmaciesRecyclerAdapter extends RecyclerView.Adapter<FavoritePharmaciesRecyclerAdapter.ViewHolder> {
    private static List<Pharmacy> localDataSet = null;

    private Context context;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public FavoritePharmaciesRecyclerAdapter(List<Pharmacy> dataSet, Context context) {
        localDataSet = dataSet;
        this.context = context;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView address;
        private final ImageView image;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.fav_pharmacy_title);
            address = view.findViewById(R.id.fav_pharmacy_description);
            image = view.findViewById(R.id.fav_pharmacy_picture);
        }

        public TextView getTitleView() {
            return title;
        }

        public TextView getAddressView() {
            return address;
        }

        public ImageView getImageView() {
            return image;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.favorite_pharmacy_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d("ClosestPharmaciesRecyclerAdapter", MessageFormat.format("{0}",localDataSet));
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).getName());
        viewHolder.getAddressView().setText(Location.getAddress(localDataSet.get(position).getLocation(), context));
        ImageUtils.loadImage(context, String.format("/pharmacies/%s", localDataSet.get(position).getId()), viewHolder.getImageView());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}