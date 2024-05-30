package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.User;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AuthUtils;

public class MyFirebaseMessagingService extends Service {

    private static final String CHANNEL_ID = "firebase_listener_channel";
    private static final int SERVICE_NOTIFICATION_ID = 1;

    Map<String, Boolean> favoritePharmaciesIds;
    Map<String, Boolean> medicineNotificationIds;

    @Override
    public void onCreate() {
        super.onCreate();
        AuthUtils.registerUserDataListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user == null) return;
                favoritePharmaciesIds = user.getFavoritePharmaciesIds();
                medicineNotificationIds = user.getMedicineNotificationIds();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        createNotificationChannel();
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification());
        setupPharmaciesListener();
    }

    private void setupPharmaciesListener() {
        DatabaseReference pharmaciesRef = FirebaseDatabase.getInstance().getReference("pharmacies");

        pharmaciesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                    setupStockListener(pharmacy.getId(), pharmacy.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private boolean isInFavoritesAndNotifications(String pharmacyId, String medicineId){

        return favoritePharmaciesIds != null && medicineNotificationIds != null &&
                favoritePharmaciesIds.get(pharmacyId) != null && medicineNotificationIds.get(medicineId) != null;
    }

    private void setupStockListener(String pharmacyID, String pharmacyName) {
        DatabaseReference stockRef = FirebaseDatabase.getInstance().getReference("pharmacies").child(pharmacyID).child("stock");
        //Ignore existing values
        AtomicBoolean initialSnapshotReceived = new AtomicBoolean(false);
        stockRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String medicineId = dataSnapshot.getKey();
                if (initialSnapshotReceived.get() &&
                        AuthUtils.isLoggedIn() && !AuthUtils.getUser().isAnonymous() && isInFavoritesAndNotifications(pharmacyID, medicineId)) {
                    String quantity = dataSnapshot.getValue(String.class);
                    DatabaseReference medicineRef = FirebaseDatabase.getInstance().getReference("medicines").child(medicineId).child("name");
                    medicineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String medicineName = dataSnapshot.getValue(String.class);
                            sendNotification("Medicine " + medicineName + " is Available!",
                                    "Pharmacy " + pharmacyName + " added " + quantity + "units of " + medicineName);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
        // Listener to detect when the initial snapshot has been received
        stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the flag to true once the initial snapshot is received
                initialSnapshotReceived.set(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.favorite_fill)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Build the notification
        Notification notification = builder.build();

        // Get the notification manager and display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(0, notification);
    }

    private Notification createServiceNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.favorite_fill)
                .setContentTitle("Firebase Listener Service")
                .setContentText("Listening for database changes...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Firebase Listener Channel";
            String description = "Channel for Firebase Listener Service";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}