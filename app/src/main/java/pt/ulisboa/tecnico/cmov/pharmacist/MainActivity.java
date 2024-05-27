package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

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
import com.google.firebase.database.FirebaseDatabase;

import pt.ulisboa.tecnico.cmov.pharmacist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesInPharmacyRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map.MapFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicineDetails;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicinesFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;

public class MainActivity extends AppCompatActivity implements MedicinesRecyclerAdapter.OnItemClickListener, MedicineDetails.MedicineDetailsBack, MedicinesInPharmacyRecyclerAdapter.OnItemClickListener{

    ActivityMainBinding binding;
    MapFragment mapFragment;
    MedicinesFragment medicinesFragment;
    Fragment current;
    Fragment details = new Fragment();

    SharedLocationViewModel sharedLocationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sharedLocationViewModel = new ViewModelProvider(this).get(SharedLocationViewModel.class);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapFragment = MapFragment.newInstance(this);
        medicinesFragment = MedicinesFragment.newInstance(this);

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
                            sharedLocationViewModel.setPharmacyId(returnedData);
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

    @Override
    public void onItemClicked(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine medicine, String origin) {
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
}