package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.RecyclerAdapterProvider;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders.ClosestPharmacyViewHolder;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicineDetails extends Fragment {

    private static final String ARG_MEDICINE = "arg_medicine";
    private Medicine mMedicine;
    private ImageView imageView;
    private ImageView qrCode;
    private RecyclerView nearestPharmacies;
    private SharedLocationViewModel sharedLocationViewModel;

    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerAdapterProvider<List<PharmacyDistance>, ClosestPharmacyViewHolder> nearestPharmaciesAdapter;

    private List<PharmacyDistance> recivedPharmacies = new ArrayList<>();
    private String origin;
    private MedicineDetailsBack callback;

    private Button notification;

    public Map<String, Boolean> medicineNotificationIds;


    public interface MedicineDetailsBack {
        void onBackButtonPressed(String origin);
    }

    public MedicineDetails(MedicineDetails.MedicineDetailsBack back) {
        // Required empty public constructor
        this.callback = back;
    }

    public static MedicineDetails newInstance(Medicine medicine, MedicineDetailsBack back, String origin) {
        MedicineDetails fragment = new MedicineDetails(back);
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEDICINE, medicine);
        fragment.setArguments(args);
        fragment.origin = origin;
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MedicineDetailsBack) {
            callback = (MedicineDetailsBack) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MedicineDetailsBack");
        }
    }

    public void setCallback(MedicineDetailsBack callback) {
        this.callback = callback;
    }

    // Call this method when you want to pass data back to the activity
    public void notifyBackButtonPressed() {
        if (callback != null) {
            callback.onBackButtonPressed(origin);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMedicine = getArguments().getParcelable(ARG_MEDICINE);
        }
        sharedLocationViewModel = new ViewModelProvider(requireActivity()).get(SharedLocationViewModel.class);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    }

    private void closestPharmacies(String medicineId) {

        //get all Pharmacies with the medicineId
        DatabaseReference pharmacyRef = FirebaseDatabase.getInstance().getReference("pharmacies");

        pharmacyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recivedPharmacies.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                    if (pharmacy != null && pharmacy.getStock() != null) {
                        if(pharmacy.getStock().get(medicineId) != null) {
                            recivedPharmacies.add(new PharmacyDistance(pharmacy,
                                    pt.ulisboa.tecnico.cmov.pharmacist.utils.Location.getDistance(sharedLocationViewModel.getLocation().getValue()
                                            , pharmacy.getLocation())));
                        }
                    }
                }
                recivedPharmacies.sort(new PharmacyDistanceComparator());
                nearestPharmaciesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateUserActions() {
        if (mMedicine == null) return;
        if(!AuthUtils.isLoggedIn() || AuthUtils.getUser().isAnonymous()) return;

        if(medicineNotificationIds.containsKey(mMedicine.getId())){
            notification.setText("Stop Notificarions");
        }else{
            notification.setText("Notify me when avaliable!");
        }
    }

    private void updateMedicine() {
        if(!AuthUtils.isLoggedIn() || AuthUtils.getUser().isAnonymous()) {
            Toast.makeText(getContext(), "You have to be logged in to get notifications", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mMedicine == null) return;

        if (!medicineNotificationIds.containsKey(mMedicine.getId())) {
            // Add to notifications
            AuthUtils.getUserRef().child("medicineNotificationIds").child(mMedicine.getId())
                    .setValue(true)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to add medicine to notifications:", task.getException());
                            Toast.makeText(getContext(), "Failed to add medicine to notifications", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "Added medicine to notifications: " + mMedicine.getId());
                        Toast.makeText(getContext(), "Medicine added to medicines", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Remove from notifications
            AuthUtils.getUserRef().child("medicineNotificationIds").child(mMedicine.getId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to remove medicine from notifications:", task.getException());
                            Toast.makeText(getContext(), "Failed to remove medicine from notifications", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "Removed medicine to notifications: " + mMedicine.getId());
                        Toast.makeText(getContext(), "Medicine removed from notifications", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicine_details, container, false);

        // Set name and purpose TextViews
        TextView nameTextView = rootView.findViewById(R.id.medicine_name);
        TextView purposeTextView = rootView.findViewById(R.id.medicine_purpose);
        nearestPharmacies = rootView.findViewById(R.id.nearest_pharmacies);
        imageView = rootView.findViewById(R.id.medicine_details_image);
        qrCode = rootView.findViewById(R.id.medicine_details_qrcode);
        notification = rootView.findViewById(R.id.notif_btn);

        Button backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                notifyBackButtonPressed();
            }
        });

        AuthUtils.registerUserDataListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                medicineNotificationIds = null;

                if (user == null) return;
                medicineNotificationIds = user.getMedicineNotificationIds();
                updateUserActions();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        notification.setOnClickListener(v -> updateMedicine());

        // Set text based on the Medicine object
        if (mMedicine != null) {
            sharedLocationViewModel.getLocation().observe(getViewLifecycleOwner(), location -> {
                for (PharmacyDistance pharmacyDistance: recivedPharmacies) {
                    pharmacyDistance.distance =
                            pt.ulisboa.tecnico.cmov.pharmacist.utils.Location.getDistance(
                                    location, pharmacyDistance.pharmacy.getLocation());
                }
                recivedPharmacies.sort(new PharmacyDistanceComparator());
                nearestPharmaciesAdapter.notifyDataSetChanged();
            });

            nearestPharmaciesAdapter = new RecyclerAdapterProvider<>(recivedPharmacies, getContext(), R.layout.closest_pharmacy_list_item, ClosestPharmacyViewHolder::new);

            nameTextView.setText(mMedicine.name);
            purposeTextView.setText(mMedicine.purpose);
            ImageUtils.loadImage(getContext(),"/medicines/" + mMedicine.id,imageView);
            qrCode.setImageBitmap(mMedicine.generateQrCode());
            mLayoutManager = new LinearLayoutManager(getActivity());
            nearestPharmacies.setLayoutManager(mLayoutManager);
            nearestPharmacies.setAdapter(nearestPharmaciesAdapter);
            closestPharmacies(mMedicine.id);
        }

        return rootView;
    }
}
