package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder> {
    private static List<Medicine> localDataSet = null;

    private Consumer<String> searchCallback;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public SearchRecyclerAdapter(List<Medicine> dataSet, Consumer<String> seachCallback) {
        localDataSet = dataSet;
        this.searchCallback = seachCallback;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleView;
        private final View rootView;

        private final Consumer<String> searchCallback;

        public ViewHolder(View view, Consumer<String> searchCallback) {
            super(view);
            // Define click listener for the ViewHolder's View
            rootView = view;
            this.searchCallback = searchCallback;
            rootView.setOnClickListener(this);
            titleView = (TextView) view.findViewById(R.id.medicine_search_title);
        }

        public TextView getTitleView() {
            return titleView;
        }

        @Override
        public void onClick(View v) {
            // Perform your action here
            // For example, you can get the clicked item position using getAdapterPosition()
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Medicine clickedMedicine = localDataSet.get(position);
                searchCallback.accept(clickedMedicine.name);
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.medicine_card_item_search, viewGroup, false);

        return new ViewHolder(view, searchCallback);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).name);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}