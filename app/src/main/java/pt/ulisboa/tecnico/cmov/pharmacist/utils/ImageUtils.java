package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.function.Consumer;

import pt.ulisboa.tecnico.cmov.pharmacist.BuildConfig;
import pt.ulisboa.tecnico.cmov.pharmacist.GlideApp;
import pt.ulisboa.tecnico.cmov.pharmacist.GlideRequest;
import pt.ulisboa.tecnico.cmov.pharmacist.GlideRequests;
import android.Manifest;

public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();
    private static Uri tempUri = null;

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
    }

    private static ActivityResultLauncher<Uri> takePhotoResultLauncher(Context context, ActivityResultCaller resultCaller, Consumer<Bitmap> onSuccess) {
        return resultCaller.registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                try {
                    onSuccess.accept(getBitmapFromUri(context, tempUri));
                } catch (IOException e) {
                    Log.w(TAG, MessageFormat.format("TakePicture failed: getBitmapFromUri: {0}", e.getMessage()));
                }
            }
            tempUri = null;
        });
    }

    private static ActivityResultLauncher<PickVisualMediaRequest> pickMediaResultLauncher(Context context, ActivityResultCaller resultCaller, Consumer<Bitmap> onSuccess) {
        return resultCaller.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    onSuccess.accept(getBitmapFromUri(context, uri));
                } catch (IOException e) {
                    Log.w(TAG, MessageFormat.format("PickVisualMedia failed: getBitmapFromUri: {0}", e.getMessage()));
                }
            }
        });
    }

    private static ImageResultLaunchers registerResultLaunchers(Context context, ActivityResultCaller resultCaller, Consumer<Bitmap> onSuccess) {
        return new ImageResultLaunchers(takePhotoResultLauncher(context, resultCaller, onSuccess), pickMediaResultLauncher(context, resultCaller, onSuccess));
    }

    public static ImageResultLaunchers registerResultLaunchers(AppCompatActivity activity, Consumer<Bitmap> onSuccess) {
        return registerResultLaunchers(activity.getApplicationContext(), activity, onSuccess);
    }

    public static ImageResultLaunchers registerResultLaunchers(Fragment fragment, Consumer<Bitmap> onSuccess) {
        return registerResultLaunchers(fragment.getContext(), fragment, onSuccess);
    }



    public static void openDialog(Context context, ImageResultLaunchers resultLaunchers) throws IOException {
        new MaterialAlertDialogBuilder(context).setTitle("Upload image")
                .setPositiveButton("Take a photo", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Check if the camera permission is already available.
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // If permission is not granted, request for permission.
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            return; // Exit the method and wait for the user to grant permission.
                        }else {
                            openImageDialog(context, resultLaunchers);
                        }
                    }
                })
                .setNegativeButton("Choose from gallery", (dialog, which) -> resultLaunchers.getPickMediaResultLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()))
                .show();
    }
    private static void openImageDialog(Context context, ImageResultLaunchers resultLaunchers) {
        try {
            tempUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File.createTempFile("tmp", ".jpg", context.getCacheDir()));
        } catch (IOException e) {
            Log.w(TAG, MessageFormat.format("Failed to create tmp file: {0}", e.getMessage()));
            Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, MessageFormat.format("Launching photos with uri: {0}", tempUri.getPath()));
        resultLaunchers.getTakePhotoResultLauncher().launch(tempUri);
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Context context, ImageResultLaunchers resultLaunchers) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // If the request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with opening the dialog.
                openImageDialog(context, resultLaunchers);
            } else {
                // Permission was denied, show a message to the user.
                Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void uploadImage(Context context, Bitmap bitmap, String bucket, String image, Consumer<StorageMetadata> onSuccess) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(bucket).child(image);
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageStream);

        imageRef.putBytes(imageStream.toByteArray()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, MessageFormat.format("Failed to upload image: {0}", task.getException()));
                Toast.makeText(context, MessageFormat.format("Failed to upload image: {0}", task.getException()), Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, MessageFormat.format("Uploaded image: {0}", imageRef.getPath()));

            onSuccess.accept(task.getResult().getMetadata());
        });
    }

    public static void loadImage(Context context, String path, int placeholder, ImageView imageView) {
        if (path.isEmpty()) return;

        GlideRequest<Drawable> request = GlideApp.with(context).load(FirebaseStorage.getInstance().getReference(path)).centerCrop();

        if (placeholder != -1) {
            request.placeholder(placeholder);
        }

        request.into(imageView);
    }

    public static void loadImage(Context context, String path, ImageView imageView) {
        loadImage(context, path, -1, imageView);
    }
}
