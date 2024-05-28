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
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.BindViewDataSetter;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.Location;

public class NotificationsViewHolder extends RecyclerView.ViewHolder implements BindViewDataSetter {
    private final TextView title;
    private final TextView description;
    private final ImageView image;

    private final Button button;
    private final Context context;
    private final List<Medicine> localDataSet;

    public NotificationsViewHolder(View view, Context context, List<Medicine> localDataSet, Consumer<String> itemClickListener) {
        super(view);
        title = view.findViewById(R.id.medicine_title);
        description = view.findViewById(R.id.medicine_description);
        image = view.findViewById(R.id.picture_medicine);
        button = view.findViewById(R.id.medicine_list_buy);
        this.localDataSet = localDataSet;
        this.context = context;

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.accept(localDataSet.get(position).getId());
            }
        });
    }

    @Override
    public void setData(int position) {
        button.setVisibility(View.GONE);
        title.setText(localDataSet.get(position).getName());
        description.setText(localDataSet.get(position).getPurpose());
        ImageUtils.loadImage(context, String.format("/medicines/%s", localDataSet.get(position).getId()), image);
    }
}
