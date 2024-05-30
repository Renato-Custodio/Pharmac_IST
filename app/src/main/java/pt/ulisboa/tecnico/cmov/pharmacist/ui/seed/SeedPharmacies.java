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

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class SeedPharmacies {
    public static void seedPharmacy(Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference pharmaciesRef = database.getReference("pharmacies");

        String pharmaciesJson = "{\n" +
                "  \"pharmacies\": [\n" +
                "    {\n" +
                "      \"name\": \"Lisbon\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7223,\n" +
                "        \"lng\": -9.1393\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"São Jorge\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7369,\n" +
                "        \"lng\": -9.1426\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Santa Maria\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7223,\n" +
                "        \"lng\": -9.1393\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Alameda\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.734,\n" +
                "        \"lng\": -9.1402\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Belem\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.6972,\n" +
                "        \"lng\": -9.2064\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Estrela\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7133,\n" +
                "        \"lng\": -9.1594\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Alvalade\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7487,\n" +
                "        \"lng\": -9.1449\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Campo Grande\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7531,\n" +
                "        \"lng\": -9.1513\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Arroios\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7262,\n" +
                "        \"lng\": -9.1346\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Bairro Alto\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7154,\n" +
                "        \"lng\": -9.1496\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Areeiro\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7403,\n" +
                "        \"lng\": -9.1391\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Ajuda\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7063,\n" +
                "        \"lng\": -9.1969\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Anjos\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7281,\n" +
                "        \"lng\": -9.1359\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Bica\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7109,\n" +
                "        \"lng\": -9.1481\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Telheiras\",\n" +
                "      \"location\": {\n" +
                "        \"lat\": 38.7572,\n" +
                "        \"lng\": -9.1618\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        Gson gson = new Gson();
        PharmacyList pharmacyList = gson.fromJson(pharmaciesJson, PharmacyList.class);

        List<Pharmacy> pharmacies = pharmacyList.getPharmacies();

        for (Pharmacy pharmacyExample : pharmacies) {
            DatabaseReference newPharmacyRef = pharmaciesRef.push();

            //create new pharmacy
            Pharmacy pharmacy = new Pharmacy();
            pharmacy.setId(newPharmacyRef.getKey());
            pharmacy.setLocation(pharmacyExample.getLocation());
            pharmacy.setName(pharmacyExample.getName());
            pharmacy.setOwner(AuthUtils.getUser().getUid());
            pharmacy.generateRandomRatings();
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.__wa3uibb2uizmvpt6reyk8g);
            newPharmacyRef.setValue(pharmacy)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Pharmacy entry added successfully with key: " + newPharmacyRef.getKey());
                        ImageUtils.uploadImage(context, bitmap, "pharmacies", newPharmacyRef.getKey(), metadata -> {});
                        Toast.makeText(context, "Pharmacy added successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add
                        Log.e("Firebase", "Failed to add medicine entry", e);
                    });
        }
    }
}
