package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.RecyclerAdapterProvider;
import pt.ulisboa.tecnico.cmov.pharmacist.ui.adapters.view_holders.NotificationsViewHolder;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.AdapterUtils;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerAdapterProvider<List<Medicine>, NotificationsViewHolder> notificationsAdapter;
    private List<Medicine> medicines = new ArrayList<>();

    private RecyclerView notificationsList;

    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private void checkEmpty() {
        if (medicines.isEmpty()) {
            findViewById(R.id.notifications_empty_card).setVisibility(View.VISIBLE);
            notificationsList.setVisibility(View.GONE);
        } else {
            notificationsList.setVisibility(View.VISIBLE);
            findViewById(R.id.notifications_empty_card).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        findViewById(R.id.notifications_back_button).setOnClickListener(e -> finish());
        notificationsList = findViewById(R.id.notifications_recycler_view);
        // Initialize medicine list
        mLayoutManager = new LinearLayoutManager(this);
        notificationsAdapter = new RecyclerAdapterProvider<>(medicines, this, R.layout.medicine_list_item, (view, context, dataset) -> new NotificationsViewHolder(view, context, dataset, this::onItemClicked));
        notificationsList.setLayoutManager(mLayoutManager);
        notificationsList.setAdapter(notificationsAdapter);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("medicineNotificationIds").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String medicineKey = snapshot.getKey();

                if (medicineKey != null) {
                    FirebaseDatabase.getInstance().getReference().child("medicines").child(medicineKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Medicine medicine = snapshot.getValue(Medicine.class);

                                medicines.add(medicine);
                                notificationsAdapter.notifyItemInserted(medicines.indexOf(medicine));
                                checkEmpty();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, MessageFormat.format("Failed to fetch pharmacy: ", error.toException()));
                        }
                    });

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                AdapterUtils.removeChild(snapshot.getKey(), medicines, notificationsAdapter);
                checkEmpty();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, MessageFormat.format("Could not fetch favorite pharmacies: {0}", error.toException()));
            }
        });

        checkEmpty();

    }

    public void onItemClicked(String medicineId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("medicineKey", medicineId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
