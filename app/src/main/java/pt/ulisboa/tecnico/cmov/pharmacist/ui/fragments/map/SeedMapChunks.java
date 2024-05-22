package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;

public class SeedMapChunks {
    public static void seedChuncks(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference pharmacyRef = database.getReference("pharmacies");

        DatabaseReference chunksRef = database.getReference("chunks");

        pharmacyRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int vez = 1;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("snapshot: " + snapshot.getValue());
                    Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                    double chunkLat = MapChunk.precisionRound(pharmacy.getLocation().lat, 50);
                    double chunkLng = MapChunk.precisionRound(pharmacy.getLocation().lng, 50);
                    String chunkId = MapChunk.getMapChunk(chunkLat,chunkLng);
                    MapChunk mapChunk = new MapChunk(new Location(chunkLat, chunkLng), chunkId);
                    Query query = chunksRef.orderByChild("chunkId").equalTo(chunkId);
                    System.out.println(vez);
                    vez++;
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    System.out.println("entrei1");
                                    MapChunk chunk = snapshot.getValue(MapChunk.class);
                                    chunk.addPharmacyId(pharmacy.getId());
                                    System.out.println(chunk.pharmaciesIDs);
                                    chunksRef.child(chunkId).setValue(chunk)
                                            .addOnSuccessListener(aVoid -> System.out.println("Chunk updated successfully"))
                                            .addOnFailureListener(e -> System.err.println("Error updating chunk: " + e.getMessage()));
                                }

                            } else {
                                System.out.println("entrei2");
                                mapChunk.addPharmacyId(pharmacy.getId());
                                chunksRef.child(chunkId).setValue(mapChunk);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.err.println("Database error: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Medicine Fragment getting medicines", "Database error: " + databaseError.getMessage());
            }
        });
    }
}
