package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicineDetails;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicinesFragment;

public class MedicinesRecyclerAdapter extends RecyclerView.Adapter<MedicinesRecyclerAdapter.ViewHolder> {
    private static List<Medicine> localDataSet = null;
    private static Fragment fragment = null;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public MedicinesRecyclerAdapter(List<Medicine> dataSet, Fragment incomingFragment) {
        localDataSet = dataSet;
        fragment = incomingFragment;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleView;
        private final TextView descriptionView;
        private final View rootView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            rootView.setOnClickListener(this);
            titleView = (TextView) view.findViewById(R.id.medicine_title);
            descriptionView = (TextView) view.findViewById(R.id.medicine_description);

        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDescriptionView() {
            return descriptionView;
        }

        @Override
        public void onClick(View v) {
            // Perform your action here
            // For example, you can get the clicked item position using getAdapterPosition()
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                Medicine clickedMedicine = localDataSet.get(position);
                // Get reference to the FragmentManager
                FragmentManager fragmentManager = fragment.requireActivity().getSupportFragmentManager();

                // Start a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Replace the current fragment with your new fragment
                MedicineDetails newFragment = MedicineDetails.newInstance(clickedMedicine);

                transaction.replace(R.id.fragmentContainerView, newFragment); // R.id.fragment_container is the id of the container where you want to replace the fragments

                // You can also add the transaction to the back stack, so the user can navigate back
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.medicine_card_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).name);
        viewHolder.getDescriptionView().setText(localDataSet.get(position).purpose);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}