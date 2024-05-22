package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

public class ImageResultLaunchers {
    private final ActivityResultLauncher<Uri> takePhotoResultLauncher;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaResultLauncher;

    public ImageResultLaunchers(ActivityResultLauncher<Uri> takePhotoResultLauncher, ActivityResultLauncher<PickVisualMediaRequest> pickMediaResultLauncher) {
        this.takePhotoResultLauncher = takePhotoResultLauncher;
        this.pickMediaResultLauncher = pickMediaResultLauncher;
    }

    public ActivityResultLauncher<Uri> getTakePhotoResultLauncher() {
        return takePhotoResultLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickMediaResultLauncher() {
        return pickMediaResultLauncher;
    }
}
