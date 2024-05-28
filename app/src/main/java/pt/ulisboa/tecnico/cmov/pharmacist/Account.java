package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.SharedLocationViewModel;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class Account extends AppCompatActivity implements Serializable {


    private void fetchUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isAnonymous()) {
                findViewById(R.id.account_user_details).setVisibility(View.GONE);
                findViewById(R.id.logout_button).setVisibility(View.GONE);
                findViewById(R.id.login_card).setVisibility(View.VISIBLE);
                findViewById(R.id.manage_pharmacies_card).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.user_name)).setText(user.isAnonymous() ? user.getUid() : user.getDisplayName());
                ImageUtils.loadImage(getApplicationContext(), String.format("/users/%s", user.getUid()), R.drawable.account_circle, findViewById(R.id.user_image));
                findViewById(R.id.login_card).setVisibility(View.GONE);
                findViewById(R.id.logout_button).setVisibility(View.VISIBLE);
                findViewById(R.id.account_user_details).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.account_progress);

        findViewById(R.id.account_back_button).setOnClickListener(e -> finish());

        findViewById(R.id.logout_button).setOnClickListener(e -> {
            linearProgressIndicator.setVisibility(View.VISIBLE);
            // Revert to an anonymous account after logging out
            AuthUtils.logout(() -> AuthUtils.signAsAnonymous(getApplicationContext(), firebaseUser -> {
                linearProgressIndicator.setVisibility(View.GONE);
                fetchUserData();
            }));
        });

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String returnedData = result.getData().getStringExtra("resultKey");
                            String data = result.getData().getStringExtra("medicineKey");
                            Intent resultIntent = new Intent();
                            if(returnedData != null){
                                resultIntent.putExtra("resultKey", returnedData);
                            } else if (data != null) {
                                resultIntent.putExtra("medicineKey", data);
                            }
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    }
                }
        );

        findViewById(R.id.account_login_button).setOnClickListener(v -> startActivity(new Intent(Account.this, AccountLogin.class)));

        findViewById(R.id.account_menu_favorites).setOnClickListener(v -> activityResultLauncher.launch(new Intent(Account.this, FavoritesActivity.class)));

        findViewById(R.id.notifications_card).setOnClickListener(v -> activityResultLauncher.launch(new Intent(Account.this, NotificationsActivity.class)));

        AuthUtils.registerUserDataListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user == null) return;

                findViewById(R.id.manage_pharmacies_card).setVisibility(user.isPharmacyOwner ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserData();
    }
}