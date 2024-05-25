package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class MedicinesRecyclerAdapter extends RecyclerView.Adapter<MedicinesRecyclerAdapter.ViewHolder> {
    private static List<Medicine> localDataSet = null;
    private static Fragment fragment = null;

    private Context context;

    public interface OnItemClickListener {
        void onItemClicked(Medicine medicine, String origin);
    }
    private final OnItemClickListener itemClickListener;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public MedicinesRecyclerAdapter(List<Medicine> dataSet, Fragment incomingFragment, OnItemClickListener listener, Context context) {
        localDataSet = dataSet;
        fragment = incomingFragment;
        itemClickListener = listener;
        this.context = context;
    }



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleView;
        private final TextView descriptionView;
        private final ImageView imageView;
        private final TextView buttonView;
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
            imageView = view.findViewById(R.id.picture_medicine);
            buttonView = view.findViewById(R.id.medicine_list_buy);
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

        public TextView getButtonView() {
            return buttonView;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Medicine clickedMedicine = localDataSet.get(position);
                itemClickListener.onItemClicked(clickedMedicine, "MedicinesRecyclerAdapter");
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
        ImageUtils.loadImage(context,"/medicines/" + localDataSet.get(position).id,viewHolder.getImageView());
        viewHolder.getButtonView().setVisibility(View.GONE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}