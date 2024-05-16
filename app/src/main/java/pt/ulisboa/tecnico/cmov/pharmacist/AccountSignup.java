package pt.ulisboa.tecnico.cmov.pharmacist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountSignup extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // Initialize buttons
        Button logIn = (Button) findViewById(R.id.loginButton);
        Button signUp = (Button) findViewById(R.id.signupButton);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRegistrationValid()){
                    Intent mainIntent = new Intent(AccountSignup.this, MainActivity.class);

                    //choose info to pass to MainActivity
                    startActivity(mainIntent);
                }
            }
        });
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(AccountSignup.this, AccountLogin.class);
                startActivity(loginIntent);
            }
        });
    }

    public boolean isRegistrationValid(){
        return true;
    }
}
