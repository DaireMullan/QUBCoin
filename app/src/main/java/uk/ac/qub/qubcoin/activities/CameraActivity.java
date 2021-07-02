package uk.ac.qub.qubcoin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import uk.ac.qub.qubcoin.R;
import uk.ac.qub.qubcoin.api.ApiLayer;
import uk.ac.qub.qubcoin.api.ApiStatus;
import uk.ac.qub.qubcoin.logging.Logging;
import uk.ac.qub.qubcoin.models.QrDbObject;
import uk.ac.qub.qubcoin.notifications.Notification;
import uk.ac.qub.qubcoin.utilities.ProgressSpinner;
import uk.ac.qub.qubcoin.viewmodels.CameraXViewModel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private final static String TAG = CameraActivity.class.getName();
    private final static int PERMISSION_CAMERA_REQUEST = 1;
    private final static int LENS_FACING = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;
    private PreviewView previewView;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    private ProgressSpinner progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        progressSpinner = new ProgressSpinner(this);
        if (isCameraPermissionGranted()) {
            setupCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                setupCamera();
            } else {
                Logging.error(TAG, "No Camera Permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupCamera() {
        progressSpinner.showProgress(false);
        previewView = findViewById(R.id.preview_view);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(LENS_FACING).build();

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            if (isCameraPermissionGranted()) {
                                bindPreviewUseCase();
                                bindAnalysisUseCase();
                            }
                        });
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        builder.setTargetAspectRatio(AspectRatio.RATIO_16_9);
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        try {
            cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Logging.error(TAG, e.getMessage());
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE)
                        .build();
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);
        analysisUseCase = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();

        analysisUseCase.setAnalyzer(
                cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        processImageProxy(barcodeScanner, imageProxy);
                    }
                }
        );

        try {
            cameraProvider.bindToLifecycle(
                    /* lifecycleOwner= */this,
                    cameraSelector,
                    analysisUseCase
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            Logging.error(TAG, e.getMessage());
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void processImageProxy(BarcodeScanner barcodeScanner, ImageProxy imageProxy) {
        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());
        barcodeScanner.process(inputImage).addOnSuccessListener(this, new OnSuccessListener<List<Barcode>>() {
            @Override
            public void onSuccess(List<Barcode> barcodes) {
                Logging.debug(TAG, "PROCESSING IMAGE");
                for (Barcode barcode : barcodes) {
                    processQrValue(barcode.getRawValue());
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logging.error(TAG, e.getMessage());
            }
        }).addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                imageProxy.close();
            }
        });
    }

    private void processQrValue(String value) {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            progressSpinner.showProgress(true);
        }
        String userUid = FirebaseAuth.getInstance().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("modules");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap snapshotValue = (HashMap) snapshot.getValue();
                if (snapshotValue == null) {
                    scanComplete("You must be enrolled on a QUB module to redeem QUBCoin");
                } else {
                    Set<String> modulesEnrolled = (snapshotValue).keySet();
                    // need to query the list of modules for the qrcode
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("modules");
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean codeFound = false;
                            try {
                                for (String moduleId : modulesEnrolled) {
                                    if (snapshot.child(moduleId).child("qrCodes").getValue() != null) {
                                        HashMap<String, QrDbObject> moduleQrCodes = (HashMap<String, QrDbObject>) snapshot.child(moduleId).child("qrCodes").getValue();
                                        if (moduleQrCodes.containsKey(value)) {
                                            codeFound = true;
                                            Logging.debug(TAG, "QR code value found");
                                            QrDbObject qrObject = snapshot.child(moduleId).child("qrCodes").child(value).getValue(QrDbObject.class);
                                            addQrToUserDb(qrObject, value);
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                Logging.error(TAG, "Error validating QR code, appears to be no modules created within the DB: " + e.getMessage());
                                scanComplete("There doesn't appear to be any QUBCoin QR codes available.");
                            }
                            if (!codeFound) {
                                scanComplete("No QUBCoin QR Code here\nYou may not be enrolled on this module.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Logging.error(TAG, "Error validating QR contents:" + error.getMessage());
                            notifyUser("Error reading QR code contents\nPlease try again");
                            setupCamera();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logging.error(TAG, "Error validating QR contents: " + error.getMessage());
                notifyUser("Error reading QR code contents\nPlease try again");
                setupCamera();
            }
        });
    }

    private void addQrToUserDb(QrDbObject qr, String value) {
        String userUid = FirebaseAuth.getInstance().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("qrCodes") && snapshot.child("qrCodes").hasChild(value)) {
                    Logging.debug(TAG, "qr code exists for this user - cannot use again");
                    scanComplete("QR code has already been redeemed \nCannot use again");
                } else {
                    // can now process transfer and add to db
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    // TODO: remove when QUBCoin accepts decimal values
                    // See [QUBCOIN-84]
                    int amount = (int) qr.getValue();
                    String amt = Integer.toString(amount);
                    ApiLayer.transfer("centralbank@qub.ac.uk", email, amt, new ApiStatus() {
                        @Override
                        public void success(JSONObject data) {
                            dbRef.child("qrCodes").child(value).setValue(true).addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    Logging.debug(TAG, "Successfully added qr to user");
                                    scanComplete("Successfully scanned QR code and earned QUBCoin!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    String message = "Failed to add the qr code to the user db - try again";
                                    Logging.error(TAG, message);
                                    notifyUser(message);
                                    setupCamera();
                                }
                            });
                        }

                        @Override
                        public void fail(JSONObject data) {
                            Logging.debug(TAG, data.toString());
                            notifyUser("QUBCoin Transfer failed, please try again");
                            setupCamera();
                        }

                        @Override
                        public void error(String message) {
                            Logging.error(TAG, message);
                            notifyUser("There was an error processing QUBCoin transfer, please try again");
                            setupCamera();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logging.error(TAG, error.getMessage());
                notifyUser("There was an error validating QUBCoin QR Code. Please try again");
                setupCamera();
            }
        });
    }

    private void notifyUser(String message) {
        Notification.toast(this, message);
    }

    private void scanComplete(String message) {
        progressSpinner.showProgress(false);
        notifyUser(message);
        finish();
    }
}
