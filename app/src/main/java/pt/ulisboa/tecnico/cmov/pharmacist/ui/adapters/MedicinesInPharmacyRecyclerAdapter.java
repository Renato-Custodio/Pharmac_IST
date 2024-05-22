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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
        viewHolder.getButtonView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference db = database.getReference("pharmacies/" + pharmacy.getId() + "/stock/Key_" + id);
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int stock = Integer.parseInt((String) dataSnapshot.getValue());

                        if(stock - 1 < 0){
                            Toast.makeText(v.getContext(), "Not enough stock!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        database.getReference("pharmacies/" + pharmacy.getId() + "/stock/Key_" + id)
                                .setValue(String.valueOf(stock - 1));
                        pharmacy.addStock("Key_" + id, stock - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Map fragment getting medicines", "Database error: " + databaseError.getMessage());
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