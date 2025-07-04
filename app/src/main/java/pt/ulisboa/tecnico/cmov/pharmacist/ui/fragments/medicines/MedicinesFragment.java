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
import android.widget.Button;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.RecyclerAdapterProvider;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.SearchRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders.MedicineViewHolder;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.NavigateFunction;


public class MedicinesFragment extends Fragment {
    private static final int PAGE_SIZE = 4;
    RecyclerView recommendedMedicines;
    RecyclerAdapterProvider<List<Medicine>, MedicineViewHolder> medicinesRecyclerAdapter;
    SearchRecyclerAdapter searchRecyclerAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    // Search view controllers

    SearchBar searchBar;
    SearchView searchView;
    RecyclerView resultsMedicines;
    RecyclerView.LayoutManager mLayoutManager2;

    // Search results
    private List<Medicine> searchResults = new ArrayList<>();
    private NavigateFunction<Medicine> openMedicine;

    // Search back button
    private Button backButton;

    public static MedicinesFragment newInstance(NavigateFunction<Medicine> openMedicine) {
        MedicinesFragment fragment = new MedicinesFragment();
        fragment.openMedicine = openMedicine;
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

    private void medicineSearch(String query, boolean initialQuery) {
        if (query.isEmpty() && !initialQuery) return;
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference medicinesRef = database.getReference("medicines");


        Query query1;
        if(query.isEmpty()){
            query1 = medicinesRef.limitToFirst(PAGE_SIZE);
        }else{
            query = query.substring(0,1).toUpperCase() + query.substring(1);
            query1 = medicinesRef.orderByChild("name").
                    startAt(query).endAt(query + "\uf8ff").limitToFirst(PAGE_SIZE);

        }

        query1.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine> medicines = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine medicine =
                            snapshot.getValue(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine.class);
                    medicines.add(medicine);
                }
                backButton.setVisibility(View.VISIBLE);
                searchResults.clear();
                searchResults.addAll(medicines);
                medicinesRecyclerAdapter.notifyDataSetChanged();
                searchRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });

    }

    private void registerSearchEvents() {
        searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                // Handle search view opened, load medicines
                medicineSearch("", true);
            }
        });

        backButton.setOnClickListener(v -> {
            searchView.setText("");
            searchBar.setText("");
            searchResults.clear();
            searchRecyclerAdapter.notifyDataSetChanged();
            backButton.setVisibility(View.GONE);
        });

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                medicineSearch(s.toString(), false);
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

        medicinesRecyclerAdapter = new RecyclerAdapterProvider<>(searchResults, getContext(), R.layout.medicine_list_item, (view1, context, dataset) -> {
            MedicineViewHolder medicineViewHolder = new MedicineViewHolder(view1, context, dataset, (medicine) -> openMedicine.apply(medicine, "MedicinesRecyclerAdapter"), (pharmacyId) -> {});
            medicineViewHolder.hideBuyButton();
            return medicineViewHolder;
        });

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
