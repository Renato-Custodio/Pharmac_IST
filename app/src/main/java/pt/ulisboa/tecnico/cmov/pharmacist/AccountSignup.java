package pt.ulisboa.tecnico.cmov.pharmacist;

import static android.util.Patterns.EMAIL_ADDRESS;
import static com.basgeekball.awesomevalidation.ValidationStyle.TEXT_INPUT_LAYOUT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageResultLaunchers;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ImageUtils;

public class AccountSignup extends AppCompatActivity {

    private static final String REGEX_PASSWORD = "(?=.*).{8,}";

    private boolean isPharmacyOwner = false;

    private Bitmap profilePhoto = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        AwesomeValidation mAwesomeValidation = new AwesomeValidation(TEXT_INPUT_LAYOUT);
        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.signup_progress);


        mAwesomeValidation.addValidation((TextInputLayout)findViewById(R.id.signup_email_input), EMAIL_ADDRESS, "Please enter a valid email");
        mAwesomeValidation.addValidation((TextInputLayout) findViewById(R.id.signup_username_input), s -> !s.isEmpty() && s.length() >= 2 && s.length() <= 20, "Username needs to be at least 2 and no more than 20 characters");
        mAwesomeValidation.addValidation((TextInputLayout) findViewById(R.id.signup_password_input), REGEX_PASSWORD, "Password needs to be at least 8 characters");

        // Initialize buttons
        Button logIn = findViewById(R.id.signup_login_button);
        Button signUp = findViewById(R.id.signup_create_account_button);
        Button uploadPhoto = findViewById(R.id.signup_upload_photo_button);

        findViewById(R.id.signup_back_button).setOnClickListener(e -> finish());

        ((RadioButton) findViewById(R.id.owner_role)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPharmacyOwner = isChecked;
        });

        ImageResultLaunchers resultLaunchers = ImageUtils.registerResultLaunchers(this, bitmap -> {
            ( (ImageView) findViewById(R.id.signup_preview_image)).setImageBitmap(bitmap);
            profilePhoto = bitmap;
        });


        uploadPhoto.setOnClickListener(v -> {
            try {
                ImageUtils.openDialog(this, resultLaunchers);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        signUp.setOnClickListener(v -> {
            if(!mAwesomeValidation.validate()) {
                return;
            }

            linearProgressIndicator.setVisibility(View.VISIBLE);

            String email = ((EditText)findViewById(R.id.signup_email_text)).getText().toString();
            String username = ((EditText)findViewById(R.id.signup_username_text)).getText().toString();
            String password = ((EditText)findViewById(R.id.password_edit_text)).getText().toString();

            AuthCredential credentials = EmailAuthProvider.getCredential(email, password);

            AuthUtils.convertToPermanentAccount(getApplicationContext(), username, isPharmacyOwner, credentials, () -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) return;

                ImageUtils.uploadImage(getApplicationContext(), profilePhoto, "users", user.getUid(), metadata -> {
                    linearProgressIndicator.setVisibility(View.GONE);
                    finish();
                });
            }, () -> linearProgressIndicator.setVisibility(View.GONE));
        });

        logIn.setOnClickListener(v -> {
            Intent loginIntent = new Intent(AccountSignup.this, AccountLogin.class);
            startActivity(loginIntent);
            finish();
        });
    }
}
