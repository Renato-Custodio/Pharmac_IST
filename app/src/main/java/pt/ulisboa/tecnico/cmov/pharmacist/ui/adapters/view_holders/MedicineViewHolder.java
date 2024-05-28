package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.BindViewDataSetter;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

public class MedicineViewHolder extends RecyclerView.ViewHolder implements BindViewDataSetter {
    private final TextView titleView;
    private final TextView descriptionView;
    private final ImageView imageView;
    private final Button buyButton;
    private final Context context;
    private final List<Medicine> localDataSet;

    public MedicineViewHolder(View view, Context context, List<Medicine> localDataSet, Consumer<Medicine> onClick, Consumer<Medicine> buyButtonOnClick) {
        super(view);

        titleView = view.findViewById(R.id.medicine_title);
        descriptionView = view.findViewById(R.id.medicine_description);
        imageView = view.findViewById(R.id.picture_medicine);
        buyButton = view.findViewById(R.id.medicine_list_buy);
        this.localDataSet = localDataSet;
        this.context = context;

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClick.accept(localDataSet.get(position));
            }
        });

        buyButton.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                buyButtonOnClick.accept(localDataSet.get(position));
            }
        });
    }

    public void hideBuyButton() {
        buyButton.setVisibility(View.GONE);
    }

    @Override
    public void setData(int position) {
        titleView.setText(localDataSet.get(position).name);
        descriptionView.setText(localDataSet.get(position).purpose);
        ImageUtils.loadImage(context,"/medicines/" + localDataSet.get(position).id, imageView);
    }
}
