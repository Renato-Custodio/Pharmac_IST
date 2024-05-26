package pt.ulisboa.tecnico.cmov.pharmacist.ui.seed;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class SeedMedicines {

    public static void seedMedicine(Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference medicinesRef = database.getReference("medicines");

        String json = "{\"medicines\":[{\"name\":\"Aspirin\",\"purpose\":\"Pain relief, fever reduction, anti-inflammatory\"},{\"name\":\"Amoxicillin\",\"purpose\":\"Antibiotic used to treat bacterial infections\"},{\"name\":\"Lisinopril\",\"purpose\":\"Blood pressure medication\"},{\"name\":\"Atorvastatin\",\"purpose\":\"Cholesterol-lowering medication\"},{\"name\":\"Ibuprofen\",\"purpose\":\"Pain relief, fever reduction, anti-inflammatory\"},{\"name\":\"Metformin\",\"purpose\":\"Antidiabetic medication for type 2 diabetes\"},{\"name\":\"Cetirizine\",\"purpose\":\"Antihistamine used to relieve allergy symptoms\"},{\"name\":\"Omeprazole\",\"purpose\":\"Proton pump inhibitor for treating acid reflux and stomach ulcers\"},{\"name\":\"Acetaminophen\",\"purpose\":\"Pain relief, fever reduction\"},{\"name\":\"Simvastatin\",\"purpose\":\"Cholesterol-lowering medication\"}]}";

        Gson gson = new Gson();
        MedicineList medicineList = gson.fromJson(json, MedicineList.class);


        List<Medicine> medicines = medicineList.getMedicines();

        for (Medicine medicineExample : medicines) {
            DatabaseReference newMedicineRef = medicinesRef.push();
            //create new medicine
            Medicine medicine = new Medicine();
            medicine.setId(newMedicineRef.getKey());
            String name = medicineExample.name;
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            medicine.setName(name);
            medicine.setPurpose(medicineExample.purpose);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.generic_medicine);
            newMedicineRef.setValue(medicine)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Medicine entry added successfully with key: " + newMedicineRef.getKey());
                        ImageUtils.uploadImage(context, bitmap, "medicines", newMedicineRef.getKey(), metadata -> {});
                        Toast.makeText(context, "Medicine added successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add
                        Log.e("Firebase", "Failed to add medicine entry", e);
                    });
        }
    }
}
