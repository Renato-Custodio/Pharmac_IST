package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Location;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.pojo.PharmacyChunkData;
import pt.ulisboa.tecnico.cmov.pharmacist.utils.ChunkUtils;

public class SeedMapChunks {
    public static void seedChuncks(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference pharmacyRef = database.getReference("pharmacies");

        DatabaseReference chunksRef = database.getReference("chunks");

        pharmacyRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                    double chunkLat = ChunkUtils.precisionRound(pharmacy.getLocation().lat, 100);
                    double chunkLng = ChunkUtils.precisionRound(pharmacy.getLocation().lng, 100);

                    String chunkId = ChunkUtils.getChunkId(chunkLat,chunkLng);

                    MapChunk mapChunk = new MapChunk(new Location(chunkLat, chunkLng), chunkId);
                    //DatabaseReference db = chunksRef.child(chunkId);
                    Query query = chunksRef.orderByChild("chunkId").equalTo(chunkId);
                    /*db.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            if (mutableData == null){
                                System.out.println("num tem nada");
                                return Transaction.abort();
                            }
                            MapChunk chunk = mutableData.getValue(MapChunk.class);
                            if (chunk == null) {
                                return Transaction.abort();
                            }

                            chunk.addPharmacy(new PharmacyChunkData(pharmacy.getId(), pharmacy.getLocation()));
                            mutableData.setValue(chunk);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Log.e("Map fragment getting medicines", "Database error: " + databaseError.getMessage());
                            }else if (!committed) {
                                // Transaction was not committed, likely due to insufficient stock
                            }else {
                                // Transaction committed and stock updated
                               System.out.println("success");
                            }
                        }
                    });*/
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            /*if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    System.out.println("entrei1");
                                    MapChunk chunk = snapshot.getValue(MapChunk.class);
                                    chunk.addPharmacy(new PharmacyChunkData(pharmacy.getId(), pharmacy.getLocation()));
                                    chunksRef.child(chunkId).setValue(chunk)
                                            .addOnSuccessListener(aVoid -> System.out.println("Chunk updated successfully"))
                                            .addOnFailureListener(e -> System.err.println("Error updating chunk: " + e.getMessage()));
                                }

                            } else {*/
                                mapChunk.addPharmacy(new PharmacyChunkData(pharmacy.getId(), pharmacy.getLocation()));
                                chunksRef.child(chunkId).setValue(mapChunk);
                            //}
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
