package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.MainActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;

public class AuthUtils {
    private static final String TAG = AuthUtils.class.getSimpleName();
    private static final List<ValueEventListener> userChangesListeners = new ArrayList<>();
    private static DatabaseReference userRef = null;

    public static boolean isLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    public static void logout(Runnable onLogout) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        onLogout.run();
    }

    public static void signAsAnonymous(Context context, Consumer<FirebaseUser> onSuccess) {
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

                User newUserData = new User("anonymous", new HashMap<>(), new HashMap<>(),new HashMap<>(), false, false);

                db.child("users").child(user.getUid()).setValue(newUserData).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        updateUserRef(user);
                        onSuccess.accept(user);
                    }
                });
            });
        } else {
            Log.d(TAG, "user already logged in");
            updateUserRef(auth.getCurrentUser());
        }
    }

    public static void signAsAnonymous(Context context) {
        signAsAnonymous(context, firebaseUser -> {});
    }

    private static void deleteUser(FirebaseUser user) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        user.delete().addOnCompleteListener(deleteFromAuthTask -> {
            if (deleteFromAuthTask.isSuccessful()) {
                db.child("users").child(user.getUid()).removeValue().addOnCompleteListener(deleteFromDbTask -> {
                    Log.d(TAG, MessageFormat.format("Deleted Anonymous User: {0}", user.getUid()));
                });
            }
        });
    }

    public static void signInWithCredentials(Context context, String email, String password, Runnable onSuccess, Runnable onFail) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser anonymousUser = auth.getCurrentUser();

        /**
         * The problem: Everytime a user logs out a new anonymous account is created
         * Why it's a problem: The database starts to fill with anonymous accounts really fast.
         * The solution: Everytime the user logs in, delete the anonymous account.
         */

        if (anonymousUser == null) return;

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                if (user == null) return;

                // Check if the user is banned
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Boolean isBanned = dataSnapshot.child("isBanned").getValue(Boolean.class);
                            if (isBanned != null && isBanned) {
                                // User is banned
                                Log.w(TAG, "signWithCredentials:failure - User is banned");
                                Toast.makeText(context, "Failed to login: User is banned", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                onFail.run();
                            } else {
                                // User is not banned, proceed with login
                                deleteUser(anonymousUser);

                                Log.d(TAG, "signWithCredentials:success");
                                Toast.makeText(context, MessageFormat.format("Logged in as {0}", user.getDisplayName()), Toast.LENGTH_SHORT).show();
                                updateUserRef(user);
                                onSuccess.run();
                            }
                        } else {
                            Log.w(TAG, "signWithCredentials:failure - User data not found");
                            Toast.makeText(context, "Failed to login: User data not found", Toast.LENGTH_SHORT).show();
                            auth.signOut();
                            onFail.run();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "signWithCredentials:failure", databaseError.toException());
                        Toast.makeText(context, "Failed to login: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        onFail.run();
                    }
                });
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context,"Failed to login: Invalid credentials", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "signWithCredentials:failure", task.getException());
                    Toast.makeText(context, "Failed to login: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                onFail.run();
            }
        });
    }

    public static DatabaseReference getUserRef() {
        return userRef;
    }

    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void convertToPermanentAccount(Context context, String username, boolean isPharmacyOwner, AuthCredential credentials, Runnable onSuccess, Runnable onFail) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

        Objects.requireNonNull(auth.getCurrentUser()).linkWithCredential(credentials).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "linkWithCredential:success");
                FirebaseUser user = task.getResult().getUser();

                if (user == null) return;

                // Update profile
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build()).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        // Update username and role
                        Log.d(TAG, "updateProfile:success");
                        db.child(user.getUid()).child("username").setValue(username);
                        db.child(user.getUid()).child("isPharmacyOwner").setValue(isPharmacyOwner);
                        onSuccess.run();
                    } else {
                        Log.w(TAG, "updateProfile:failure", t.getException());
                        Toast.makeText(context, MessageFormat.format("Failed to create account: {0}", t.getException().getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w(TAG, "linkWithCredential:failure", task.getException());
                Toast.makeText(context, MessageFormat.format("Failed to create account: {0}", task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                onFail.run();
            }
        });
    }

    private static void updateUserRef(FirebaseUser user) {
        if (userRef != null) {
            userChangesListeners.forEach(valueEventListener -> userRef.removeEventListener(valueEventListener));
        }

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        userChangesListeners.forEach(valueEventListener -> userRef.addValueEventListener(valueEventListener));

    }

    /**
     * This method ensures persistence of event listeners after logins/logouts
     * @param valueEventListener
     */
    public static void registerUserDataListener(ValueEventListener valueEventListener) {
        userChangesListeners.add(valueEventListener);

        if (userRef != null) {
            userRef.addValueEventListener(valueEventListener);
        }
    }

    public static void removeUserDataListener(ValueEventListener valueEventListener) {
        userChangesListeners.remove(valueEventListener);

        if (userRef != null) {
            userRef.removeEventListener(valueEventListener);
        }
    }

    public static void bannedUserAlert(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        logout(() -> {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Banned User")
                        .setMessage("A significant number of pharmacies you own have been suspended resulting in this account's indefinite ban.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                            activity.finish();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            });
        });
    }
}
