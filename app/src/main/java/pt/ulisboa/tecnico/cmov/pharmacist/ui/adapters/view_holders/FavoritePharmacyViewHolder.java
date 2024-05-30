package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.BindViewDataSetter;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

/**
 * Provide a reference to the type of views that you are using
 * (custom ViewHolder)
 */

public class FavoritePharmacyViewHolder extends RecyclerView.ViewHolder implements BindViewDataSetter {
    private final TextView title;
    private final TextView address;
    private final ImageView image;
    private final Context context;
    private final List<Pharmacy> localDataSet;

    public FavoritePharmacyViewHolder(View view, Context context, List<Pharmacy> localDataSet, Consumer<Pharmacy> itemClickListener) {
        super(view);
        title = view.findViewById(R.id.fav_pharmacy_title);
        address = view.findViewById(R.id.fav_pharmacy_description);
        image = view.findViewById(R.id.fav_pharmacy_picture);
        this.localDataSet = localDataSet;
        this.context = context;

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.accept(localDataSet.get(position));
            }
        });
    }

    @Override
    public void setData(int position) {
        title.setText(localDataSet.get(position).getName());
        address.setText(Location.getAddress(localDataSet.get(position).getLocation(), context));
        ImageUtils.loadImage(context, String.format("/pharmacies/%s", localDataSet.get(position).getId()), image);
    }
}