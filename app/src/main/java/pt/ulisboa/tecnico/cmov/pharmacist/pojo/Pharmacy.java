package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;
@IgnoreExtraProperties
public class Pharmacy implements Indexed {

    private String id;
    private String name;
    private Location location;
    //key = medicineId // value = quantity
    private Map<String, Object> stock;

    public Pharmacy(){}

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
}
