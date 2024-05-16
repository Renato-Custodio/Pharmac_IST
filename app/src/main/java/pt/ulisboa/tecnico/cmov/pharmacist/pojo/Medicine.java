package pt.ulisboa.tecnico.cmov.pharmacist.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Medicine implements Parcelable {

    public String id;

    public String name;

    public String purpose;

    public String picture;

    // Constructor
    public Medicine(String id, String name, String purpose, String picture) {
        this.id = id;
        this.name = name;
        this.purpose = purpose;
        this.picture = picture;
    }

    // Default no-argument constructor
    public Medicine() {
    }

    public String getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    // Parcelable implementation
    protected Medicine(Parcel in) {
        id = in.readString();
        name = in.readString();
        purpose = in.readString();
        picture = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(purpose);
        dest.writeString(picture);
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Medicine> CREATOR = new Creator<Medicine>() {
        @Override
        public Medicine createFromParcel(Parcel in) {
            return new Medicine(in);
        }

        @Override
        public Medicine[] newArray(int size) {
            return new Medicine[size];
        }
    };
}
