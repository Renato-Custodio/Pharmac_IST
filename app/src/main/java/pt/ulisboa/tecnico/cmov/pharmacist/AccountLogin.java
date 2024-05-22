package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;

public class AccountLogin extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        // Initialize buttons
        Button logIn = findViewById(R.id.login_button);
        Button signUp = findViewById(R.id.createAccountButton);

        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.login_progress);

        findViewById(R.id.login_back_button).setOnClickListener(e -> finish());

        logIn.setOnClickListener(v -> {

            String email = ((EditText)findViewById(R.id.login_email_text)).getText().toString();
            String password = ((EditText)findViewById(R.id.login_password_text)).getText().toString();

            linearProgressIndicator.setVisibility(View.VISIBLE);

            AuthUtils.signInWithCredentials(getApplicationContext(), email, password, () -> {
                linearProgressIndicator.setVisibility(View.GONE);
                finish();
            }, () -> {
                linearProgressIndicator.setVisibility(View.GONE);
            });
        });

        signUp.setOnClickListener(v -> {
            Intent signupIntent = new Intent(AccountLogin.this, AccountSignup.class);
            startActivity(signupIntent);
            finish();
        });
    }
}