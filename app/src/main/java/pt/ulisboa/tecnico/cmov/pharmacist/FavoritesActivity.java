package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.FavoritePharmaciesRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AdapterUtils;


public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager mLayoutManager;
    private FavoritePharmaciesRecyclerAdapter favoritePharmaciesListAdapter;
    private List<Pharmacy> pharmacies = new ArrayList<>();

    private RecyclerView favoritePharmaciesList;

    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private void checkEmpty() {
        if (pharmacies.isEmpty()) {
            findViewById(R.id.favorites_empty_card).setVisibility(View.VISIBLE);
            favoritePharmaciesList.setVisibility(View.GONE);
        } else {
            favoritePharmaciesList.setVisibility(View.VISIBLE);
            findViewById(R.id.favorites_empty_card).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        findViewById(R.id.favorites_back_button).setOnClickListener(e -> finish());

        favoritePharmaciesList = findViewById(R.id.favorites_recycler_view);

        // Initialize pharmacies list
        mLayoutManager = new LinearLayoutManager(this);
        favoritePharmaciesListAdapter = new FavoritePharmaciesRecyclerAdapter(pharmacies, this);
        favoritePharmaciesList.setLayoutManager(mLayoutManager);
        favoritePharmaciesList.setAdapter(favoritePharmaciesListAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("favoritePharmaciesIds").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String pharmacyKey = snapshot.getKey();

                if (pharmacyKey != null) {
                    FirebaseDatabase.getInstance().getReference().child("pharmacies").child(pharmacyKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);

                                pharmacies.add(pharmacy);
                                favoritePharmaciesListAdapter.notifyItemInserted(pharmacies.indexOf(pharmacy));
                                checkEmpty();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, MessageFormat.format("Failed to fetch pharmacy: ", error.toException()));
                        }
                    });

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                AdapterUtils.removeChild(snapshot.getKey(), pharmacies, favoritePharmaciesListAdapter);
                checkEmpty();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, MessageFormat.format("Could not fetch favorite pharmacies: {0}", error.toException()));
            }
        });

        checkEmpty();

    }


}
