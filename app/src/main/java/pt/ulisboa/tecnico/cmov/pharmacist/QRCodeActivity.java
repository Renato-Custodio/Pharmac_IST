package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.CaptureActivity;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;

import android.widget.Toast;

public class QRCodeActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> qrCodeScanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ActivityResultLauncher
        qrCodeScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String medicineId = data.getStringExtra("SCAN_RESULT");
                        if (medicineId == null){
                            finish();
                        }
                        medicineId = medicineId.replace("\"", "");
                        System.out.println("sacned medicine " + medicineId);
                        //add stock view

                    } else {
                        // create medicine View
                        System.out.println("RIP");
                    }
                    finish(); // Finish the activity after handling the QR code result
                });

        // Start the QR code scanner
        startScanner();
    }

    private void startScanner() {
        Intent intent = new Intent(this, CaptureActivity.class);
        qrCodeScanLauncher.launch(intent);
    }
}


