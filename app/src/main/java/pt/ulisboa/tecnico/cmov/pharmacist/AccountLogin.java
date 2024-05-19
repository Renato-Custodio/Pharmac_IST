package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountLogin extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        // Initialize buttons
        Button logIn = findViewById(R.id.loginButton);
        Button signUp = findViewById(R.id.createAccountButton);

        findViewById(R.id.login_back_button).setOnClickListener(e -> finish());

        logIn.setOnClickListener(v -> {
            if(isLoginValid()){
                Intent mainIntent = new Intent(AccountLogin.this, MainActivity.class);
                //choose info to pass to MainActivity
                startActivity(mainIntent);
                finish();
            }
        });

        signUp.setOnClickListener(v -> {
            Intent signupIntent = new Intent(AccountLogin.this, AccountSignup.class);
            startActivity(signupIntent);
            finish();
        });
    }

    public boolean isLoginValid(){
        return true;
    }
}