package pt.ulisboa.tecnico.cmov.pharmacist.client.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Medicine implements Parcelable {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("purpose")
    public String purpose;

    @SerializedName("picture")
    public String picture;

    // Constructor
    public Medicine(String id, String name, String purpose, String picture) {
        this.id = id;
        this.name = name;
        this.purpose = purpose;
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
