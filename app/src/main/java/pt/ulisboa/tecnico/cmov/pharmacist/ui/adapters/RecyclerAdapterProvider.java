package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;

public class RecyclerAdapterProvider<D extends Collection, T extends RecyclerView.ViewHolder & BindViewDataSetter> extends RecyclerView.Adapter<T> {
    private D localDataSet = null;

    private Context context;

    private final int resource;

    private final ViewHolderBuilderFunction<D, T> viewHolderConstructor;


    public RecyclerAdapterProvider(D dataSet, Context context, int resource, ViewHolderBuilderFunction<D, T> viewHolderConstructor) {
        localDataSet = dataSet;
        this.context = context;
        this.resource = resource;
        this.viewHolderConstructor = viewHolderConstructor;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public T onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(this.resource, viewGroup, false);

        return viewHolderConstructor.apply(view, context, localDataSet);
    }

    @Override
    public void onBindViewHolder(@NonNull T viewHolder, int position) {
        viewHolder.setData(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}