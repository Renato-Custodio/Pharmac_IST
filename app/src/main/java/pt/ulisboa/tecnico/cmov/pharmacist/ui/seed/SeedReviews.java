package pt.ulisboa.tecnico.cmov.pharmacist.ui.seed;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Map;

public class SeedReviews {
    public static void seedReviews(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reviewsRef = database.getReference("reviews");

        String reviewsJson = "{\n" +
                "  \"scores\": {\n" +
                "    \"pharmacy1\": {\n" +
                "      \"user1\": 4,\n" +
                "      \"user2\": 5\n" +
                "    },\n" +
                "    \"pharmacy2\": {\n" +
                "      \"user1\": 3,\n" +
                "      \"user2\": 2\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Gson gson = new Gson();
        ReviewsList reviewsList = gson.fromJson(reviewsJson, ReviewsList.class);

        Map<String, Map<String, Integer>> reviews = reviewsList.getScores();

        for (Map.Entry<String, Map<String, Integer>> entry : reviews.entrySet()) {
            String pharmacyId = entry.getKey();
            Map<String, Integer> userRatings = entry.getValue();

            DatabaseReference newReviewRef = reviewsRef.child(pharmacyId);
            newReviewRef.setValue(userRatings)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Reviews entry added successfully for pharmacy: " + pharmacyId);
                        //Toast.makeText(context, "Reviews added successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add
                        Log.e(TAG, "Failed to add reviews entry for pharmacy: " + pharmacyId, e);
                    });
        }
    }
}
