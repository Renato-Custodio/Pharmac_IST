package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.APIFactory;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.ClosestPharmaciesRecyclerAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.transition.MaterialSharedAxis;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MedicineDetails extends Fragment {

    private static final String ARG_MEDICINE = "arg_medicine";
    private Medicine mMedicine;

    private RecyclerView nearestPharmacies;
    private SharedLocationViewModel sharedLocationViewModel;

    private RecyclerView.LayoutManager mLayoutManager;

    private ClosestPharmaciesRecyclerAdapter nearestPharmaciesAdapter;

    private List<PharmacyDistance> recivedPharmacies = new ArrayList<PharmacyDistance>();

    public interface back {
        void back();
    }
    private final back back;
    public MedicineDetails(MedicineDetails.back back) {
        // Required empty public constructor
        this.back = back;
    }

    public static MedicineDetails newInstance(Medicine medicine, back back) {
        MedicineDetails fragment = new MedicineDetails(back);
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEDICINE, medicine);
        fragment.setArguments(args);
        return fragment;
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

    private void closestPharmacies(String medicineId, String lat, String lng) {
        Call<List<PharmacyDistance>> closestPharmacies = APIFactory.getInterface().doGetClosestPharmacies(medicineId, lat, lng);
        closestPharmacies.enqueue(new Callback<List<PharmacyDistance>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<PharmacyDistance>> call, Response<List<PharmacyDistance>> response) {
                recivedPharmacies.clear();
                recivedPharmacies.addAll(response.body());
                System.out.println(recivedPharmacies);
                nearestPharmaciesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<PharmacyDistance>> call, Throwable t) {
                Log.e("closestPharmacies", t.getMessage());
                call.cancel();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicine_details, container, false);

        // Set name and purpose TextViews
        TextView nameTextView = rootView.findViewById(R.id.medicine_name);
        TextView purposeTextView = rootView.findViewById(R.id.medicine_purpose);
        //TextView imageTextView = rootView.findViewById(R.id.imageView);
        nearestPharmacies = rootView.findViewById(R.id.nearest_pharmacies);

        Button backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                back.back();
            }
        });

        // Set text based on the Medicine object
        if (mMedicine != null) {
            Location currentLocation = sharedLocationViewModel.getLocation();
            nearestPharmaciesAdapter = new ClosestPharmaciesRecyclerAdapter(recivedPharmacies, getContext());
            nameTextView.setText(mMedicine.name);
            purposeTextView.setText(mMedicine.purpose);
            Picasso.get().load(MessageFormat.format("{0}/images/{1}", BuildConfig.SERVER_BASE_URL, mMedicine.picture)).into((ImageView) rootView.findViewById(R.id.medicine_details_image));
            mLayoutManager = new LinearLayoutManager(getActivity());
            nearestPharmacies.setLayoutManager(mLayoutManager);
            nearestPharmacies.setAdapter(nearestPharmaciesAdapter);

            closestPharmacies(mMedicine.id,String.valueOf((int) Math.round(currentLocation.getLatitude())),
                    String.valueOf((int) Math.round(currentLocation.getLongitude())));
        }

        return rootView;
    }
}
