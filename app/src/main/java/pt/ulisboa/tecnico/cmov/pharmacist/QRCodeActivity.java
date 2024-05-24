package pt.ulisboa.tecnico.cmov.pharmacist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;

public class QRCodeActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private Button scanButton;

    private Button backButton;

    private TextView medicineName;

    private  TextView medicineDescription;

    private TextView currentStock;

    private EditText addStock;

    private Button addStockButton;

    private String pharmacyId;

    private Context context;

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
        //add stock fields
        medicineName = findViewById(R.id.medicine_name_scan);
        medicineDescription = findViewById(R.id.medicine_purpose_scan);
        currentStock = findViewById(R.id.stock_scan);
        /*stockDisplay = findViewById(R.id.stock_display);
        addStock = findViewById(R.id.add_stock_field);
        addStockButton = findViewById(R.id.add_stock_button);*/
        //set views GONE
        /*stockDisplay.setVisibility(View.GONE);
        addStock.setVisibility(View.GONE);
        addStockButton.setVisibility(View.GONE);*/
        // Set click listener for backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set click listener for scanButton
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medicineName.setVisibility(View.GONE);
                medicineDescription.setVisibility(View.GONE);
                scanButton.setVisibility(View.GONE);
                startScanner();
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
                    String medicineId = result.getText().replace("\"", "");
                    System.out.println("Scanned data: " + medicineId);
                    getMedicine(medicineId);
                    getCurrentStock(medicineId);
                } else {
                    System.out.println("No barcode found");
                }
                stopScanner();
                scanButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getCurrentStock(String medicineId){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("pharmacies/" + pharmacyId + "/stock/Key_" + medicineId);
        db.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                String stockStr = mutableData.getValue(String.class);
                if (stockStr == null) {
                    return Transaction.abort();
                }
                currentStock.setText(stockStr);
                currentStock.setVisibility(View.VISIBLE);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {}
        });

    }

    public void updateStock(String medicineId, int value){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("pharmacies/" + pharmacyId + "/stock/Key_" + medicineId);
        db.runTransaction(new Transaction.Handler() {
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
                    int newStock = Integer.parseInt(dataSnapshot.getValue(String.class));
                    Toast.makeText(context, "stock updated successfully! Current stock: " + newStock, Toast.LENGTH_SHORT).show();
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
