package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@IgnoreExtraProperties
public class Pharmacy implements Indexed {

    private String id;
    private String name;
    private Location location;
    //key = medicineId // value = quantity
    private Map<String, Object> stock;
    private Map<String, Long> ratings;

    public Pharmacy(){
        Map<String, Long> initialRatings = new HashMap<>();
        initialRatings.put("rating_1", 0L);
        initialRatings.put("rating_2", 0L);
        initialRatings.put("rating_3", 0L);
        initialRatings.put("rating_4", 0L);
        initialRatings.put("rating_5", 0L);
        this.ratings = initialRatings;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    public void addStock(String medicineId, int quantity){
        this.stock.put(medicineId, String.valueOf(quantity));
    }

    public Map<String, Object> getStock() {
        return stock;
    }

    public void setStock(Map<String, Object> stock) {
        this.stock = stock;
    }

    public int getMedicineStock(String medicineId){
        return Integer.parseInt((String) this.stock.get(medicineId)) ;
    }

    // Function to generate random ratings for a pharmacy
    public void generateRandomRatings() {
        Random random = new Random();
        // Generate random values for each rating category
        this.ratings.put("rating_1", (long) random.nextInt(100));
        this.ratings.put("rating_2", (long) random.nextInt(100));
        this.ratings.put("rating_3", (long) random.nextInt(100));
        this.ratings.put("rating_4", (long) random.nextInt(100));
        this.ratings.put("rating_5", (long) random.nextInt(100));
    }
}
