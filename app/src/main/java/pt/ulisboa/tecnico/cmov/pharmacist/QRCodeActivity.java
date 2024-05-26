package pt.ulisboa.tecnico.cmov.pharmacist;

import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyDistance;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.medicines.PharmacyDistanceComparator;

public class QRCodeActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private Button scanButton;

    private Button backButton;

    private TextView medicineName;

    private  TextView medicineDescription;

    private TextView currentStock;

    private TextView addStockMessage;

    private Button addStockButton;

    private TextInputEditText addStockText;

    private String pharmacyId;

    private Context context;

    private String medicineId;

    private TextInputLayout addStockLayout;

    private Button createMedicineButton;

    private String stock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        context = this;
        pharmacyId = getIntent().getStringExtra("pharmacyId");

        // Initialize views
        barcodeView = findViewById(R.id.medicine_details_scan);
        scanButton = findViewById(R.id.scan_button);
        backButton = findViewById(R.id.backButton_scan);
        //medicine and stock info fields
        medicineName = findViewById(R.id.medicine_name_scan);
        medicineDescription = findViewById(R.id.medicine_purpose_scan);
        currentStock = findViewById(R.id.stock_scan);
        //add stock fields
        addStockMessage = findViewById(R.id.stock_message);
        addStockButton = findViewById(R.id.add_stock_button);
        addStockText = findViewById(R.id.add_stock_text);
        addStockLayout = findViewById(R.id.add_stock_input);
        createMedicineButton = findViewById(R.id.create_medicine_button);
        // Set click listener for backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeView.pause();
                scanButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(new Intent(context, CreateMedicineActivity.class));
                activityResultLauncher.launch(intent);
            }
        });

        // Set click listener for scanButton
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStockLayout.setVisibility(View.GONE);
                addStockMessage.setVisibility(View.GONE);
                addStockButton.setVisibility(View.GONE);
                addStockText.setVisibility(View.GONE);
                medicineName.setVisibility(View.GONE);
                medicineDescription.setVisibility(View.GONE);
                scanButton.setVisibility(View.GONE);
                currentStock.setVisibility(View.GONE);
                startScanner();
            }
        });
        initTextValidation();
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String medicine = data.getStringExtra("medicineId");
                            String amount = data.getStringExtra("quantity");
                            createStock(medicine, amount);
                        }
                    }
                }
            }
    );

    private void createStock(String medicine ,String amount){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("pharmacies").child(pharmacyId).child("stock");

        // Create a new map entry
        Map<String, Object> stockEntry = new HashMap<>();
        stockEntry.put(medicine, amount);

        // Update the value in Firebase
        db.updateChildren(stockEntry, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("Stock update", "Database error: " + databaseError.getMessage());
                    Toast.makeText(context, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Stock updated successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initTextValidation(){
        AwesomeValidation mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        mAwesomeValidation.addValidation(addStockLayout, (String s) -> {
            try {
                return Integer.parseInt(s) > 0;
            }catch (NumberFormatException e){
                return false;
            }
        } , "Amount has to be greater than 0");
        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate params
                if(!mAwesomeValidation.validate()) {
                    return;
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(Objects.requireNonNull(addStockText.getText()).toString());
                }catch (Exception e){
                    return;
                }
                addStockText.getText().clear();
                addStockText.clearFocus();
                if(stock == null){
                    createStock(medicineId, String.valueOf(amount));
                }else{
                    System.out.println(stock);
                    updateStock(medicineId, amount);
                }

            }
        });
    }

    private void stopScanner() {
        barcodeView.pause();
    }

    private void startScanner() {
        barcodeView.resume();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    medicineId = result.getText().replace("\"", "");
                    getMedicine(medicineId);
                    getCurrentStock(medicineId);
                    addStockMessage.setVisibility(View.VISIBLE);
                    addStockButton.setVisibility(View.VISIBLE);
                    addStockLayout.setVisibility(View.VISIBLE);
                    addStockText.setVisibility(View.VISIBLE);
                } else {
                    System.out.println("No barcode found");
                    //create medicine
                }
                stopScanner();
                scanButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getCurrentStock(String medicineId){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference db = database.getReference("pharmacies").child(pharmacyId).child("stock").child(medicineId);

        db.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stock = dataSnapshot.getValue(String.class);
                String text = "Stock: " + stock;
                currentStock.setText(text);
                currentStock.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void updateStock(String medicineId, int value){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("pharmacies").child(pharmacyId).child("stock").child(medicineId);
        db.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                String stockStr = mutableData.getValue(String.class);
                if (stockStr == null) {
                    return Transaction.success(mutableData);
                }

                int stock = Integer.parseInt(stockStr);

                // Update stock
                mutableData.setValue(String.valueOf(stock + value));
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("Map fragment getting medicines", "Database error: " + databaseError.getMessage());
                    Toast.makeText(context, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // Transaction committed and stock updated
                    int newStock = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue(String.class)));
                    Toast.makeText(context, "Stock updated successfully! Current stock: " + newStock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getMedicine(String medicineId){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference medicinesRef = database.getReference("medicines").child(medicineId);
        medicinesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine medicine =
                        dataSnapshot.getValue(pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine.class);
                medicineName.setText(medicine.name);
                medicineDescription.setText(medicine.purpose);
                medicineName.setVisibility(View.VISIBLE);
                medicineDescription.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });
    }
}
