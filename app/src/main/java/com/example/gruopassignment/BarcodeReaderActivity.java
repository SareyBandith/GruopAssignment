package com.example.gruopassignment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;

public class BarcodeReaderActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 3000;
    private Uri imageFileUri;
    private ImageView imageView;
    private TextView textViewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barcode_reader);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        //Where is the image and text need to show
        imageView = findViewById(R.id.imageViewBarcodeReader);
        textViewOutput = findViewById(R.id.textViewOpenCameraorGallery);
    }
    //Check Permission for Open Camera
    private boolean checkPermission() {
        String permission = android.Manifest.permission.CAMERA;
        boolean grantCamera = ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
        if (!grantCamera) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    REQUEST_PERMISSION);
        }
        return grantCamera;
    }

    //Onclicked On Camera
    public void openCamera(View view) {
        if (checkPermission() == false)
            return;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFileUri =
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new
                        ContentValues());
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        activityResultLauncher.launch(takePhotoIntent);
    }
    //Onclicked on LoadImage button
    public void loadImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(galleryIntent);
    }
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                if (result.getData() != null &&
                                        result.getData().getData() != null)
                                    imageFileUri = result.getData().getData();
                                imageView.setImageURI(imageFileUri);
                                // Add code for ML Kit below this line
                                textViewOutput.setText("");
                                InputImage image = null;
                                try {
                                    image = InputImage.fromFilePath(getBaseContext(), imageFileUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (image != null) {
                                    processImageFromBarcodeReader(image);
                                }
                            }
                        }
                    });
    public void processImageFromBarcodeReader (InputImage image) {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        textViewOutput.append(Html.fromHtml("<font color='navy'><b>Detected barcode:</b></font><br>", Html.FROM_HTML_MODE_LEGACY));
                        String result = "";
                        for (Barcode barcode : barcodes) {
                            result = barcode.getRawValue();
                            textViewOutput.append(result + "\n");
                        }
                        if (result.length() < 2) {
                            textViewOutput.append(" Barcode not found.\n");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewOutput.setText("Failed");
                    }
                });
    }

}