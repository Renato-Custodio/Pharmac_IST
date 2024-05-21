package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;

public class Account extends AppCompatActivity {

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
                Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.account_circle).into((ImageView) findViewById(R.id.user_image));
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

        findViewById(R.id.account_back_button).setOnClickListener(e -> finish());

        findViewById(R.id.logout_button).setOnClickListener(e -> {
            AuthUtils.logout();
            fetchUserData();
        });

        findViewById(R.id.account_login_button).setOnClickListener(v -> {
            startActivity(new Intent(Account.this, AccountLogin.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchUserData();
    }
}