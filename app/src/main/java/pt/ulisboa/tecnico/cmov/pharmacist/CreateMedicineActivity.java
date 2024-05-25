package pt.ulisboa.tecnico.cmov.pharmacist;

import static android.content.ContentValues.TAG;
import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageResultLaunchers;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class CreateMedicineActivity extends AppCompatActivity {

    private Button backButton;
    private Button uploadPhoto;
    private TextInputEditText nameText;
    private TextInputEditText purposeText;
    private TextInputEditText quantityText;

    private Button saveMedicine;

    private TextInputLayout nameLayout;
    private TextInputLayout purposeLayout;
    private TextInputLayout quantityLayout;

    private ImageView imageView;

    private Bitmap medicinePhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_medicine);
        //init views
        backButton = findViewById(R.id.create_medicine_back_button);
        nameText = findViewById(R.id.medicine_name_text);
        purposeText = findViewById(R.id.medicine_purpose_text);
        quantityText = findViewById(R.id.create_add_stock_text);
        nameLayout = findViewById(R.id.medicine_name_input);
        purposeLayout = findViewById(R.id.medicine_purpose_input);
        quantityLayout = findViewById(R.id.create_add_stock_input);
        uploadPhoto = findViewById(R.id.medicine_upload_photo_button);
        imageView = findViewById(R.id.picture_medicine);
        saveMedicine = findViewById(R.id.save_medicine_button);
        backButton.setOnClickListener(v -> finish());

        initTextValidation();
        addPhoto();
    }

    private void addPhoto(){
        ImageResultLaunchers imageResultLaunchers = ImageUtils.registerResultLaunchers(this, bitmap -> {
            imageView.setImageBitmap(bitmap);
            medicinePhoto = bitmap;
        });

        uploadPhoto.setOnClickListener(v -> {
            try {
                ImageUtils.openDialog(this, imageResultLaunchers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initTextValidation(){
        AwesomeValidation mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);

        mAwesomeValidation.addValidation(nameLayout,s -> !s.isEmpty(), "Field cannot be empty");
        mAwesomeValidation.addValidation(purposeLayout,s -> !s.isEmpty(), "Field cannot be empty");
        mAwesomeValidation.addValidation(quantityLayout, (String s) -> {
            try {
                return Integer.parseInt(s) > 0;
            }catch (NumberFormatException e){
                return false;
            }
        } , "Amount has to be greater than 0");

        saveMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate params
                if(!mAwesomeValidation.validate()) {
                    return;
                }
                String name = Objects.requireNonNull(nameText.getText()).toString();
                String purpose = Objects.requireNonNull(purposeText.getText()).toString();
                int amount = 0;
                try {
                    amount = Integer.parseInt(Objects.requireNonNull(quantityText.getText()).toString());
                }catch (Exception e){
                    return;
                }
                Objects.requireNonNull(nameText.getText()).clear();
                Objects.requireNonNull(purposeText.getText()).clear();
                quantityText.getText().clear();
                nameText.clearFocus();
                purposeText.clearFocus();
                quantityText.clearFocus();
                imageView.setImageBitmap(null);
                saveMedicine(name, purpose);
                //updateStock(medicineId, amount);
            }
        });
    }

    private void saveMedicine(String name, String purpose){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference medicinesRef = database.getReference("medicines");

        if (medicinePhoto == null) {
            Toast.makeText(this, "Please select a picture", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference newMedicineRef = medicinesRef.push();
        //create new medicine
        Medicine medicine = new Medicine();
        medicine.setId(newMedicineRef.getKey());
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        medicine.setName(name);
        medicine.setPurpose(purpose);


        newMedicineRef.setValue(medicine)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Medicine entry added successfully with key: " + newMedicineRef.getKey());
                    ImageUtils.uploadImage(this, medicinePhoto, "medicines", newMedicineRef.getKey(), metadata -> {});
                    Toast.makeText(this, "Medicine added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add
                    Log.e("Firebase", "Failed to add medicine entry", e);
                });

    }
}
