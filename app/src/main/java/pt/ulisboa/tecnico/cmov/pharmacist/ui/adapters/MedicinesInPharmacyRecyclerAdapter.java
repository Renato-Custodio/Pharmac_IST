package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;

public class MedicinesInPharmacyRecyclerAdapter extends RecyclerView.Adapter<MedicinesInPharmacyRecyclerAdapter.ViewHolder> {
    private static List<Medicine> localDataSet = null;
    private static Fragment fragment = null;

    private static Pharmacy pharmacy = null;

    public interface OnItemClickListener {
        void onItemClicked(Medicine medicine);
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public MedicinesInPharmacyRecyclerAdapter(List<Medicine> dataSet, Fragment incomingFragment, Pharmacy inPharmacy) {
        localDataSet = dataSet;
        fragment = incomingFragment;
        pharmacy = inPharmacy;
    }



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleView;
        private final TextView descriptionView;
        private final ImageView imageView;
        private final Button BuyButton;
        private final View rootView;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            rootView.setOnClickListener(this);
            titleView = view.findViewById(R.id.medicine_title);
            descriptionView = view.findViewById(R.id.medicine_description);
            imageView = view.findViewById(R.id.picture_medicine);
            BuyButton = view.findViewById(R.id.medicine_list_buy);
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
            return BuyButton;
        }

        @Override
        public void onClick(View v) {
            // Perform your action here
            // For example, you can get the clicked item position using getAdapterPosition()
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Medicine clickedMedicine = localDataSet.get(position);
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.medicine_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTitleView().setText(localDataSet.get(position).name);
        viewHolder.getDescriptionView().setText(localDataSet.get(position).purpose);
        Picasso.get().load(MessageFormat.format("{0}/images/{1}", BuildConfig.SERVER_BASE_URL, localDataSet.get(position).picture)).into(viewHolder.getImageView());
        final String id = localDataSet.get(position).id;
        final String name = localDataSet.get(position).name;
        viewHolder.getButtonView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference db = database.getReference("pharmacies/" + pharmacy.getId() + "/stock/Key_" + id);
                db.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            String stockStr = mutableData.getValue(String.class);
                            if (stockStr == null) {
                                return Transaction.success(mutableData); // No stock information
                            }

                            int stock = Integer.parseInt(stockStr);

                            if (stock - 1 < 0) {
                                // Insufficient stock
                                return Transaction.abort();
                            }

                            // Update stock
                            mutableData.setValue(String.valueOf(stock - 1));
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Log.e("Map fragment getting medicines", "Database error: " + databaseError.getMessage());
                                Toast.makeText(v.getContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            } else if (!committed) {
                                // Transaction was not committed, likely due to insufficient stock
                                Toast.makeText(v.getContext(), "Not available!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Transaction committed and stock updated
                                int newStock = Integer.parseInt(dataSnapshot.getValue(String.class));
                                pharmacy.addStock("Key_" + id, newStock);
                                Toast.makeText(v.getContext(), "1 dose of " + name + " bought successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}