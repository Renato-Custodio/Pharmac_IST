package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;

public class AuthUtils {

    private static boolean loggedIn = false;

    private static final String TAG = "AuthUtils";

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
    }

    public static void signAsAnonymous(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // Login as anonymous initially
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(context, "Failed to login",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "signInAnonymously:success");

                // Create account data in the DB
                FirebaseUser user = auth.getCurrentUser();

                User newUserData = new User("anonymous", "", new ArrayList<>(), new ArrayList<>(), false);

                db.child("users").child(user.getUid()).setValue(newUserData).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        loggedIn = true;
                    }
                });
            });
        } else {
            Log.d(TAG, "user already logged in");
            loggedIn = true;
        }
    }

    public static void convertToPermanentAccount(Context context, String username, boolean isPharmacyOwner, AuthCredential credentials, Consumer<FirebaseUser> onSuccess) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

        auth.getCurrentUser().linkWithCredential(credentials).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TAG", "linkWithCredential:success");
                FirebaseUser user = task.getResult().getUser();

                // Update profile
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build()).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        // Update username and role
                        db.child(user.getUid()).child("username").setValue(username);
                        db.child(user.getUid()).child("isPharmacyOwner").setValue(isPharmacyOwner);
                        onSuccess.accept(user);
                    } else {
                        Log.w(TAG, "updateProfile:failure", task.getException());
                        Toast.makeText(context, "Failed to create account",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w(TAG, "linkWithCredential:failure", task.getException());
                Toast.makeText(context, "Failed to create account",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
