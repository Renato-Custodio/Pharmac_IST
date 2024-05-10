package pt.ulisboa.tecnico.cmov.pharmacist.client;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.MapChunk;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.client.pojo.PharmacyDistance;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {
    @GET("/api/medicine/search?")
    Call<List<Medicine>> doGetMedicines(@Query("query") String query, @Query("page") Integer page);

    @GET("/api/pharmacy/mapChunks?")
    Call<List<MapChunk>> doGetMapFragment(@Query("lat") double latitude, @Query("lng") double longitude);

    @GET("/api/medicine/{medicineId}/closestPharmacies?")
    Call<List<PharmacyDistance>> doGetClosestPharmacies(
            @Path("medicineId") String medicineId,
            @Query("lat") String latitude,
            @Query("lng") String longitude
    );
}
