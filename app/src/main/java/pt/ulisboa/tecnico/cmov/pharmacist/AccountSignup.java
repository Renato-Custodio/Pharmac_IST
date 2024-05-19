package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountSignup extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // Initialize buttons
        Button logIn = (Button) findViewById(R.id.signup_login_button);
        Button signUp = (Button) findViewById(R.id.signup_create_account_button);

        findViewById(R.id.signup_back_button).setOnClickListener(e -> finish());

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRegistrationValid()){
                    Intent mainIntent = new Intent(AccountSignup.this, MainActivity.class);
                    //choose info to pass to MainActivity
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(AccountSignup.this, AccountLogin.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    public boolean isRegistrationValid(){
        return true;
    }
}
