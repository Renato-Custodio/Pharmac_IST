package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.transition.MaterialFadeThrough;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesRecyclerAdapter;


public class MedicinesFragment extends Fragment implements OnItemC {

    RecyclerView recommendedMedicines;
    RecyclerView resultsMedicines;
    MedicinesRecyclerAdapter medicinesRecyclerAdapter;

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.LayoutManager mLayoutManager2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnterTransition(new MaterialFadeThrough());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medicines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> test = List.of("Medicine1", "Medicine2", "Medicine3", "Medicine2", "Medicine3", "Medicine2", "Medicine3");


        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager2 = new LinearLayoutManager(getActivity());

        recommendedMedicines = view.findViewById(R.id.recommended_medicines);
        resultsMedicines = view.findViewById(R.id.medicine_search_results);

        medicinesRecyclerAdapter = new MedicinesRecyclerAdapter(test);

        recommendedMedicines.setLayoutManager(mLayoutManager);
        resultsMedicines.setLayoutManager(mLayoutManager2);

        recommendedMedicines.setAdapter(medicinesRecyclerAdapter);
        resultsMedicines.setAdapter(medicinesRecyclerAdapter);



    }
}