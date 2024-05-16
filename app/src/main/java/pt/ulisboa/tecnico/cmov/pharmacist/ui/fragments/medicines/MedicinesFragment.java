package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.transition.MaterialFadeThrough;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.SearchRecyclerAdapter;


public class MedicinesFragment extends Fragment {

    RecyclerView recommendedMedicines;
    MedicinesRecyclerAdapter medicinesRecyclerAdapter;
    SearchRecyclerAdapter searchRecyclerAdapter;

    RecyclerView.LayoutManager mLayoutManager;

    // Search view controllers

    SearchBar searchBar;
    SearchView searchView;
    RecyclerView resultsMedicines;
    RecyclerView.LayoutManager mLayoutManager2;

    // Search results
    private List<pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine> searchResults = new ArrayList<>();
    private MedicinesRecyclerAdapter.OnItemClickListener listener;

    // Search back button
    private Button backButton;

    public static MedicinesFragment newInstance(MedicinesRecyclerAdapter.OnItemClickListener listener) {
        MedicinesFragment fragment = new MedicinesFragment();
        fragment.listener = listener;
        return fragment;
    }

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

    /*private void medicineSearch(String query, Integer page, boolean initialQuery) {
        if (query.isEmpty() && !initialQuery) return;

        Call<List<Medicine>> search = APIFactory.getInterface().doGetMedicines(query, page);
        search.enqueue(new Callback<List<Medicine>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Medicine>> call, Response<List<Medicine>> response) {
                backButton.setVisibility(View.VISIBLE);
                searchResults.clear();
                searchResults.addAll(response.body());
                medicinesRecyclerAdapter.notifyDataSetChanged();
                searchRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Medicine>> call, Throwable t) {
                Log.e("medicineSearch", t.getMessage());
                call.cancel();
            }
        });
    }*/

    private void registerSearchEvents() {
        searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                // Handle search view opened, load medicines
                //medicineSearch("", 1, true);
            }
        });

        /*backButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                searchView.setText("");
                searchBar.setText("");
                searchResults.clear();
                searchRecyclerAdapter.notifyDataSetChanged();
                backButton.setVisibility(View.GONE);
            }
        });*/

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //medicineSearch(s.toString(), 1, false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchView
                .getEditText()
                .setOnEditorActionListener(
                        (v, actionId, event) -> {
                            searchBar.setText(searchView.getText());
                            searchView.hide();
                            return false;
                        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.medicine_search_view);
        searchBar = view.findViewById(R.id.medicine_search_bar);
        backButton = view.findViewById(R.id.back_search_button);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager2 = new LinearLayoutManager(getActivity());

        recommendedMedicines = view.findViewById(R.id.nearest_pharmacies);
        resultsMedicines = view.findViewById(R.id.medicine_search_results);

        medicinesRecyclerAdapter = new MedicinesRecyclerAdapter(searchResults, this, listener);
        recommendedMedicines.setLayoutManager(mLayoutManager);
        recommendedMedicines.setAdapter(medicinesRecyclerAdapter);

        searchRecyclerAdapter = new SearchRecyclerAdapter(searchResults, (query) -> {
            searchView.setText(query);
            searchBar.setText(searchView.getText());
            searchView.hide();
        });

        resultsMedicines.setLayoutManager(mLayoutManager2);
        resultsMedicines.setAdapter(searchRecyclerAdapter);

        registerSearchEvents();

    }
}
