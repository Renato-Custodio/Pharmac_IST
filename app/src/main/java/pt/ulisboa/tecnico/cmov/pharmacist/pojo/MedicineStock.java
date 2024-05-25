package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

public class MedicineStock {
    public String id;

    public String name;
    public Integer amount;

    public MedicineStock() {}

    public MedicineStock(String id, String name, Integer amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }
}
