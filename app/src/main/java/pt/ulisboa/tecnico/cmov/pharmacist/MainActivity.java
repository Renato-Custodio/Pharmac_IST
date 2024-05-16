package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;

import pt.ulisboa.tecnico.cmov.pharmacist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.MedicinesRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map.MapFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicineDetails;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.MedicinesFragment;

public class MainActivity extends AppCompatActivity implements MedicinesRecyclerAdapter.OnItemClickListener, MedicineDetails.back{

    ActivityMainBinding binding;
    MapFragment mapFragment;
    MedicinesFragment medicinesFragment;
    Fragment current;
    Fragment details = new Fragment();

    int medicinesOrDetails = 0;

    boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapFragment = new MapFragment();
        medicinesFragment = MedicinesFragment.newInstance(this);

        addFragment(mapFragment);
        addFragment(medicinesFragment);

        replaceFragment(mapFragment);

        binding.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.account) {
                    // Handle click on the "Account" menu item
                    if (isLoggedIn) {
                        // If the user is logged in, navigate to AccountInfo activity
                        //Intent accIntent = new Intent(MainActivity.this, AccountInfo.class);
                        //startActivity(accIntent);
                        Log.d("MainActivity", "User is logged in");
                    } else {
                        // If the user is not logged in, navigate to AccountLogin activity
                        Intent accIntent = new Intent(MainActivity.this, AccountLogin.class);
                        startActivity(accIntent);
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
                    if(medicinesOrDetails == 0){
                        replaceFragment(medicinesFragment);
                    }else{
                        if(!(current == details)) {
                            addFragment(details);
                            replaceFragment(details);
                        }
                    }

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
        if(fragment == details){
            this.medicinesOrDetails = 1;
        } else if (fragment == medicinesFragment) {
            this.medicinesOrDetails = 0;
        }
        if(current == details && details != fragment) {
            transaction.remove(details);
        }
        current = fragment;
        transaction.commit();
    }

    @Override
    public void onItemClicked(pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine medicine) {
        MedicineDetails newFragment = MedicineDetails.newInstance(medicine, this);
        details = newFragment;
        addFragment(newFragment);
        replaceFragment(newFragment);
    }

    @Override
    public void back() {
        replaceFragment(medicinesFragment);
    }
}