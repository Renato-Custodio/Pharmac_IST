package pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters;

import android.content.Context;
import android.view.View;

@FunctionalInterface
public interface ViewHolderBuilderFunction<D, R> {
    R apply(View view, Context context, D localDataSet);
}
