package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;

public class MedicinesRecyclerAdapter extends RecyclerView.Adapter<MedicinesRecyclerAdapter.ViewHolder> {
    private static List<Medicine> localDataSet = null;
    private static Fragment fragment = null;

    public interface OnItemClickListener {
        void onItemClicked(Medicine medicine);
    }
    private final OnItemClickListener itemClickListener;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public MedicinesRecyclerAdapter(List<Medicine> dataSet, Fragment incomingFragment, OnItemClickListener listener) {
        localDataSet = dataSet;
        fragment = incomingFragment;
        itemClickListener = listener;
    }



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleView;
        private final TextView descriptionView;

        private final ImageView imageView;
        private final View rootView;

        private final OnItemClickListener itemClickListener;

        public ViewHolder(View view, OnItemClickListener itemClickListener) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            this.itemClickListener = itemClickListener;
            rootView.setOnClickListener(this);
            titleView = view.findViewById(R.id.medicine_title);
            descriptionView = view.findViewById(R.id.medicine_description);
            imageView = view.findViewById(R.id.medicine_image);
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDescriptionView() {
            return descriptionView;
        }

        public ImageView getImageView() {
            return imageView;
        }

        @Override
        public void onClick(View v) {
            // Perform your action here
            // For example, you can get the clicked item position using getAdapterPosition()
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Medicine clickedMedicine = localDataSet.get(position);
                itemClickListener.onItemClicked(clickedMedicine);
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.medicine_list_item, viewGroup, false);

        return new ViewHolder(view, itemClickListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).name);
        viewHolder.getDescriptionView().setText(localDataSet.get(position).purpose);
        Picasso.get().load(MessageFormat.format("{0}/images/{1}", BuildConfig.SERVER_BASE_URL, localDataSet.get(position).picture)).into(viewHolder.getImageView());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}