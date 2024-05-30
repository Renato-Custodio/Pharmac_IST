package pt.ulisboa.tecnico.cmov.pharmacist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map.MapFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicineDetails;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicinesFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.seed.SeedPharmacies;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;

public class MainActivity extends AppCompatActivity implements MedicineDetails.MedicineDetailsBack{

    ActivityMainBinding binding;
    MapFragment mapFragment;
    MedicinesFragment medicinesFragment;
    Fragment current;
    Fragment details = new Fragment();

    SharedLocationViewModel sharedLocationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EdgeToEdge.enable(this);
        sharedLocationViewModel = new ViewModelProvider(this).get(SharedLocationViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapFragment = MapFragment.newInstance(this::openMedicineDetails);
        medicinesFragment = MedicinesFragment.newInstance(this::openMedicineDetails);

        addFragment(mapFragment);
        addFragment(medicinesFragment);

        replaceFragment(mapFragment);

        //SeedMedicines.seedMedicine(this);
        //SeedPharmacies.seedPharmacy(this);
        //SeedMapChunks.seedChuncks();
        //SeedReviews.seedReviews(this);

        AuthUtils.signAsAnonymous(this);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String returnedData = result.getData().getStringExtra("resultKey");
                            String data = result.getData().getStringExtra("medicineKey");
                            if(returnedData != null) {
                                sharedLocationViewModel.setPharmacyId(returnedData);
                                binding.bottomNavigation.getMenu().getItem(0).setChecked(true);
                                replaceFragment(mapFragment);
                            } else if (data != null) {
                                getMedicine(data);
                            }
                        }
                    }
                }
        );

        binding.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.account) {
                    // Handle click on the "Account" menu item
                    if (AuthUtils.isLoggedIn()) {
                        // If the user is logged in, navigate to AccountInfo activity
                        Intent accIntent = new Intent(MainActivity.this, Account.class);
                        activityResultLauncher.launch(accIntent);
                        Log.d("MainActivity", "User is logged in");
                    } else {
                        Log.d("MainActivity", "User is not logged in");
                    }
                }
                return true;
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int menuItemId =  menuItem.getItemId();

                if (menuItemId == R.id.pharmacies_map) {
                    replaceFragment(mapFragment);
                } else if (menuItemId == R.id.medicines) {
                    replaceFragment(medicinesFragment);
                }
                return true;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Intent serviceIntent = new Intent(this, MyFirebaseMessagingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.fragmentContainerView, fragment);
        transaction.hide(fragment);
        transaction.commit();
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (current != null) transaction.hide(current);
        transaction.show(fragment);

        if(current == details && details != fragment) {
            transaction.remove(details);
        }
        current = fragment;
        transaction.commit();
    }


    public void openMedicineDetails(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine medicine, String origin) {
        MedicineDetails newFragment = MedicineDetails.newInstance(medicine, this, origin);
        details = newFragment;
        binding.bottomNavigation.getMenu().getItem(1).setChecked(true);
        addFragment(newFragment);
        replaceFragment(newFragment);
    }

    @Override
    public void onBackButtonPressed(String origin) {
        if(origin.equals("MedicinesRecyclerAdapter")){
            replaceFragment(medicinesFragment);
        }else {
            binding.bottomNavigation.getMenu().getItem(0).setChecked(true);
            replaceFragment(mapFragment);
        }
    }

    public void getMedicine(String medicineId){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference medicinesRef = database.getReference("medicines").child(medicineId);
        medicinesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Medicine medicine = dataSnapshot.getValue(Medicine.class);
                openMedicineDetails(medicine, "MedicinesRecyclerAdapter");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });
    }
}