package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;

import com.google.android.material.transition.MaterialFadeThrough;

public class MedicineDetails extends Fragment {

    private static final String ARG_MEDICINE = "arg_medicine";
    private Medicine mMedicine;

    public MedicineDetails() {
        // Required empty public constructor
    }

    public static MedicineDetails newInstance(Medicine medicine) {
        MedicineDetails fragment = new MedicineDetails();
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
        setEnterTransition(new MaterialFadeThrough());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicine_details, container, false);

        // Set name and purpose TextViews
        TextView nameTextView = rootView.findViewById(R.id.medicine_name);
        TextView purposeTextView = rootView.findViewById(R.id.medicine_purpose);
        //TextView imageTextView = rootView.findViewById(R.id.imageView);

        Button backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });

        // Set text based on the Medicine object
        if (mMedicine != null) {
            nameTextView.setText(mMedicine.name);
            purposeTextView.setText(mMedicine.purpose);
            //falta a imagem
            //logica das farmacias mais proximas
        }

        return rootView;
    }
}
